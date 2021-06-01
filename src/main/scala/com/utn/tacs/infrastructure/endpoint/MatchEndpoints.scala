package com.utn.tacs.infrastructure.endpoint

import cats.data.EitherT
import com.utn.tacs.domain.`match`.SocketMessage
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
    case GET -> Root / matchId / "room" => {
      val _ = matchId
      case class State(messageCount: Int)
      for {
        wsResponse <- WebSocketBuilder[F].build(
          t.subscribe(10).drop(1) merge q.dequeue,
          handleFrame(t, q)
        )
      } yield (wsResponse)
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
          case (Right(user2), Some(_deck)) => service.createMatch(player1, user2, _deck).value
          case _ => Left(MatchAlreadyExistsError).pure[F]
        }
      } yield result
      action.flatMap {
        case Right(value) => Ok(value.asJson)
        case Left(MatchAlreadyExistsError) => InternalServerError()
      }
  }
  private val withdrawMatchEndpoint: AuthEndpoint[F, Auth] = {
    case req@PUT -> Root / matchId / "withdraw" asAuthed _ =>
      val loserPlayer = req.identity
      val action: F[Either[MatchNotFoundError.type, Match]] = for {
        result <- service.withdraw(matchId, loserPlayer).value
      } yield result
      action.flatMap {
        case Left(MatchNotFoundError) => NotFound()
        case Right(m) => Accepted(m.asJson)
      }
  }
  private val getMatchReplayEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / matchId / "replay" asAuthed _ =>
      service.getPlayedRounds(matchId) match {
        case Some(rounds) => Ok(Json.obj(("rounds", rounds.asJson)))
        case None => NotFound()
      }
  }

  implicit val createMatchDecoder: EntityDecoder[F, CreateMatchDTO] = jsonOf

  def endpoints(): HttpRoutes[F] = {
    val authEndpoints = Auth.allRoles(
      getMatchByIdEndpoint
        .orElse(createMatchEndpoint)
        .orElse(withdrawMatchEndpoint)
        .orElse(getMatchReplayEndpoint)
    )
    connectToMatchRoom <+>
      auth.liftService(authEndpoints)
  }

  implicit val withdrawMatchDecoder: EntityDecoder[F, WithdrawMatchDto] = jsonOf

  def handleFrame(topic: Topic[F, WebSocketFrame], msgQueue: Queue[F, WebSocketFrame]): Pipe[F, WebSocketFrame, Unit] = _.evalMap {
    case Text(text, _) =>
      (for {
        json <- parse(text)
        message <- json.as[SocketMessage]
      } yield message) match {
        case Left(error) =>
          msgQueue.enqueue1(Text(error.toString))
        case Right(message) =>
          (topic.publish1 _ compose transformMessage compose stampMessage) (message)
      }
    case frame =>
      msgQueue.enqueue1(Text(s"Cannot handle the frame ${frame.opcode}"))
  }

  def stampMessage(msg: SocketMessage): SocketMessage = msg.copy(timestamp = Some(Instant.now.toEpochMilli))

  def transformMessage(msg: SocketMessage): WebSocketFrame = Text(msg.asJson.noSpaces)

  case class CreateMatchDTO(player1: String, player2: String, deckId: Int)

  case class WithdrawMatchDto(loserPlayer: String)
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