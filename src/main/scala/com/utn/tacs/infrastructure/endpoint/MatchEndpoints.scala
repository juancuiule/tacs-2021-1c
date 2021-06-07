package com.utn.tacs.infrastructure.endpoint

import cats.data.{EitherT, OptionT}
import cats.effect.{Concurrent, Sync, Timer}
import cats.syntax.all._
import com.utn.tacs.domain.`match`.{InputMessage, Match, MatchAlreadyExistsError, MatchNotFoundError, MatchService, OutputMessage, _}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.deck.DeckService
import com.utn.tacs.domain.user.{User, UserNotFoundError, UserService}
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo

import scala.util.Right

class MatchEndpoints[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
  service: MatchService[F],
  userService: UserService[F],
  deckService: DeckService[F],
  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
  //  q: Queue[F, InputMessage],
  t: Topic[F, OutputMessage]
) extends Http4sDsl[F] {
  implicit def decoder[T[_] : Sync]: EntityDecoder[T, Match] = jsonOf[T, Match]

  private val getMatchByIdEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / matchId asAuthed _ => {
      val result: EitherT[F, MatchNotFoundError.type, Match] = service.getMatch(matchId)
      result.value.flatMap {
        case Left(MatchNotFoundError) => NotFound()
        case Right(m) => Ok(m.asJson)
      }
    }
  }
  private val connectToMatchRoom = HttpRoutes.of[F] {
    case req@GET -> Root / matchId / "room" :? TokenQueryParamMatcher(accessToken) => {
      // Pone el token como header de la request
      // hacemos esto porque la API de sockets de js no manda headers
      val userFromReq: OptionT[F, User] = auth.authenticator.extractAndValidate(req.putHeaders(buildBearerAuthHeader(accessToken))).map(_.identity)

      userFromReq.value.flatMap {
        case Some(user) => {
          for {
            theMatch <- service.getMatch(matchId).value
            resp <- theMatch match {
              case Left(_) => Response[F](status = NotFound).pure[F]
              case Right(m) if m.hasPlayer(user.id.get) => {
                val toClient: Stream[F, WebSocketFrame.Text] =
                  t
                    .subscribe(10)
                    .filter(_.forUser(user.id.get, m.matchId))
                    .map(msg => Text(msg.toString))

                val inputPipe: Pipe[F, WebSocketFrame, Unit] = processInput(user, m, t)
                for {
                  wsResponse <- WebSocketBuilder[F].build(toClient, inputPipe)
                } yield wsResponse
              }
              case _ => Response[F](status = Unauthorized).pure[F]
            }
          } yield resp
        }
        case None => Response[F](status = Unauthorized).pure[F]
      }
    }
  }
  private val createMatchEndpoint: AuthEndpoint[F, Auth] = {
    // TODO: validar que no este jugando partida contra él mismo
    case req@POST -> Root asAuthed _ =>
      val action: F[Either[MatchAlreadyExistsError.type, Match]] = for {
        post <- req.request.as[CreateMatchDTO]
        player1 = req.identity
        player2 <- userService.getUserByName(post.player2).value
        deck <- deckService.get(post.deckId).value
        result <- (player2, deck) match {
          case (Right(user2), Some(_deck)) =>
            if (user2.id.contains(player1.id.get))
              service.createMatch(player1.id.get, user2.id.get, _deck.id).value
            else
              ??? // player1 == player2 no se puede jugar
          case (Right(_), None) => ??? // no existe ese deck
          case (Left(UserNotFoundError), _) => ??? // no existe este otro usuario
          case _ => Left(MatchAlreadyExistsError).pure[F]
        }
      } yield result
      action.flatMap {
        case Right(value) => Ok(value.asJson)
        case Left(MatchAlreadyExistsError) => InternalServerError()
      }
  }
  private val getMatchReplayEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / matchId / "replay" asAuthed _ =>
      service.getPlayedRounds(matchId) match {
        case Some(rounds) => Ok(Json.obj(("rounds", rounds.asJson)))
        case None => NotFound()
      }
  }

  def endpoints(): HttpRoutes[F] = {
    val authEndpoints = Auth.allRoles(
      getMatchByIdEndpoint
        .orElse(createMatchEndpoint)
        .orElse(getMatchReplayEndpoint)
    )
    connectToMatchRoom <+>
      auth.liftService(authEndpoints)
  }

  private def processInput(user: User, m: Match, t: Topic[F, OutputMessage])(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit] = {
    wsfStream.collect {
      case Text(text, _) => InputMessage._parse(text, user.id.get, m.matchId)
      case Close(_) => Disconnect(user.id.get, m.matchId)
    }.evalMap(im => {
      val action = for {
        currM <- service.getMatch(im.theMatch)
        updated <- im match {
          case GetMatch(_, _) => service.noop(currM)
          case Battle(user, _, key) if service.playerCanBattle(currM, user) => service.battleByAttribute(key)(currM)
          case Withdraw(user, _) => service.withdraw(user)(currM)
          case Disconnect(_, _) => service.noop(currM)
          case InvalidInput(_, _) => service.noop(currM)
          case _ => service.noop(currM)
        }
      } yield updated
      action.value.flatMap({
        case Left(MatchNotFoundError) => t.publish1(SendToUser(user.id.get, None))
        case Right(newMatch) => im match {
          case GetMatch(_, _) => t.publish1(SendToUser(user.id.get, newMatch.some))
          case Battle(_, _, _) => t.publish1(SendToUsers(newMatch.players, newMatch.some))
          case Withdraw(_, _) => t.publish1(SendToUsers(newMatch.players, newMatch.some))
          case Disconnect(_, _) => t.publish1(SendToUsers(newMatch.players, newMatch.some))
          case InvalidInput(_, _) => t.publish1(SendToUser(user.id.get, None))
          case _ => t.publish1(SendToUser(user.id.get, None))
        }
      })
    })
  }


  implicit val createMatchDecoder: EntityDecoder[F, CreateMatchDTO] = jsonOf

  case class CreateMatchDTO(player2: String, deckId: Int)

  object TokenQueryParamMatcher extends QueryParamDecoderMatcher[String]("access-token")
}

object MatchEndpoints {
  def apply[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
    service: MatchService[F],
    userService: UserService[F],
    deckService: DeckService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
    //    q: Queue[F, InputMessage],
    t: Topic[F, OutputMessage]
  ): HttpRoutes[F] =
    new MatchEndpoints[F, Auth](service, userService, deckService, auth, t).endpoints()
}