package com.utn.tacs.infrastructure.endpoint

import cats.data.{EitherT, OptionT}
import com.utn.tacs.domain.`match`.MatchAction.{Battle, Withdraw}
import com.utn.tacs.domain.`match`.SocketMessage
import org.http4s.Response
//import com.utn.tacs.{FromClient, ToClient}
import fs2.Pipe
import org.http4s.websocket.WebSocketFrame.Text

import java.time.Instant

//import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync, Timer}
import cats.syntax.all._
import com.utn.tacs.domain.`match`.{Match, MatchAlreadyExistsError, MatchNotFoundError, MatchService}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.deck.DeckService
import com.utn.tacs.domain.user.{User, UserService}
import fs2.concurrent.{Queue, Topic}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo

class MatchEndpoints[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
  service: MatchService[F],
  userService: UserService[F],
  deckService: DeckService[F],
  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
  q: Queue[F, WebSocketFrame],
  t: Topic[F, WebSocketFrame]
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
    case r@GET -> Root / matchId / "room" :? TokenQueryParamMatcher(accessToken) => {
      // Pone el token como header de la request
      // hacemos esto porque la API de sockets de js no manda headers
      val x: OptionT[F, User] = auth.authenticator.extractAndValidate(r.putHeaders(buildBearerAuthHeader(accessToken))).map(_.identity)

      x.value.flatMap {
        case Some(user) => {
          // TODO: usar matchId y user
          println(user.asJson)
          println(matchId)
          for {
            wsResponse <- WebSocketBuilder[F].build(
              t.subscribe(10).drop(1) merge q.dequeue,
              handleFrame(t, q)
            )
          } yield (wsResponse)
        }
        case None => Response[F](status = Unauthorized).pure[F]
      }
    }
  }
  private val createMatchEndpoint: AuthEndpoint[F, Auth] = {
    case req@POST -> Root asAuthed _ =>
      val action: F[Either[MatchAlreadyExistsError.type, Match]] = for {
        post <- req.request.as[CreateMatchDTO]
        player1 = req.identity
        player2 <- userService.getUserByName(post.player2).value
        deck <- deckService.get(post.deckId).value
        result <- (player2, deck) match {
          case (Right(user2), Some(_deck)) => service.createMatch(player1.id.get, user2.id.get, _deck.id).value
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

  implicit val createMatchDecoder: EntityDecoder[F, CreateMatchDTO] = jsonOf

  def handleFrame(topic: Topic[F, WebSocketFrame], msgQueue: Queue[F, WebSocketFrame]): Pipe[F, WebSocketFrame, Unit] = _.evalMap {
    case Text(text, _) =>
      (for {
        json <- parse(text)
        message <- json.as[SocketMessage]
      } yield message) match {
        case Left(e) => msgQueue.enqueue1(Text(e.toString))
        case Right(message) =>
          message._parse() match {
            case Left(e2) => msgQueue.enqueue1(Text(e2.toString))
            case Right(matchAction) => {

              val action: EitherT[F, MatchNotFoundError.type, Match] = for {
                theMatch <- service.getMatch(message.matchId)
                updated <- (matchAction match {
                  case Battle(cardAttribute) => service.battleByAttribute(cardAttribute)
                  case Withdraw(loser) => service.withdraw(loser)
                  case _ => service.noop
                }) (theMatch)
              } yield (updated)
              action.value.flatMap {
                case Left(MatchNotFoundError) => msgQueue.enqueue1(Text("match not found"))
                case Right(newMatch) => (topic.publish1 _ compose transformMessage compose stampMessage) (SocketMessage(
                  newMatch.matchId,
                  message.author,
                  message.action,
                  payload = newMatch.currentState.asJson.toString()
                ))
              }
            }
          }
      }
    case frame =>
      msgQueue.enqueue1(Text(s"Cannot handle the frame ${frame.opcode}"))
  }

  implicit val withdrawMatchDecoder: EntityDecoder[F, WithdrawMatchDto] = jsonOf

  def stampMessage(msg: SocketMessage): SocketMessage = msg.copy(timestamp = Some(Instant.now.toEpochMilli))

  def transformMessage(msg: SocketMessage): WebSocketFrame = Text(msg.asJson.noSpaces)

  case class CreateMatchDTO(player1: String, player2: String, deckId: Int)

  case class WithdrawMatchDto(loserPlayer: String)

  object TokenQueryParamMatcher extends QueryParamDecoderMatcher[String]("access-token")
}

object MatchEndpoints {
  def apply[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
    service: MatchService[F],
    userService: UserService[F],
    deckService: DeckService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
    q: Queue[F, WebSocketFrame],
    t: Topic[F, WebSocketFrame]
  ): HttpRoutes[F] =
    new MatchEndpoints[F, Auth](service, userService, deckService, auth, q, t).endpoints()
}