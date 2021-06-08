package com.utn.tacs.infrastructure.endpoint

import cats.data.OptionT
import cats.effect.{Concurrent, Sync, Timer}
import cats.syntax.all._
import com.utn.tacs.domain.`match`.{InputMessage, Match, MatchNotFoundError, MatchService, OutputMessage, _}
import com.utn.tacs.domain.user.User
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import io.circe.generic.auto._
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo

import scala.util.Right

class MatchRoomEndpoints[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
  service: MatchService[F],
  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
  topic: Topic[F, OutputMessage]
) extends Http4sDsl[F] {
  implicit def decoder[T[_] : Sync]: EntityDecoder[T, Match] = jsonOf[T, Match]

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
                  topic
                    .subscribe(10)
                    .filter(_.forUser(user.id.get, m.matchId))
                    .map(msg => Text(msg.toString))

                val inputPipe: Pipe[F, WebSocketFrame, Unit] = processInput(user, m, topic)
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

  def endpoints(): HttpRoutes[F] = {
    connectToMatchRoom
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

  object TokenQueryParamMatcher extends QueryParamDecoderMatcher[String]("access-token")
}

object MatchRoomEndpoints {
  def apply[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
    service: MatchService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
    topic: Topic[F, OutputMessage]
  ): HttpRoutes[F] =
    new MatchRoomEndpoints[F, Auth](service, auth, topic).endpoints()
}