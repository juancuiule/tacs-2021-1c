package com.utn.tacs

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ConcurrentEffect, ExitCode, IO, IOApp, Sync, Timer}
import fs2.concurrent.{Queue, Topic}
import fs2.{INothing, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.ExecutionContext.global

case class State(messageCount: Int)
case class FromClient(userName: String, message: String)
case class ToClient(message: String)
object Algo {
  def chatRoutes[F[_]: Sync : Concurrent](
    q: Queue[F, FromClient],
    t: Topic[F, ToClient],
  ) : HttpRoutes[F]= {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "ws" / userName =>
        val toClient = t
          .subscribe(1000)
          .map(toClientMessage =>
            WebSocketFrame.Text(toClientMessage.message)
          )

        WebSocketBuilder[F].build(toClient, _.collect({
          case WebSocketFrame.Text(text, _) =>
            FromClient(userName, text)
        })
          .through(q.enqueue)
        )
    }
  }
}

object ChatServer {
  def stream[F[_]: ConcurrentEffect](q: Queue[F, FromClient], t: Topic[F, ToClient])(implicit T: Timer[F]): Stream[F, INothing] = {
    val httpApp = Algo.chatRoutes(q, t).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)
    for {
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8081, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    //        Server.createServer.use(_ => IO.never).as(ExitCode.Success)
    for (
      q <- Queue.unbounded[IO, FromClient];
      t <- Topic[IO, ToClient](ToClient("==="));
      ref <- Ref.of[IO, State](State(1));

      exitCode <- {
        val messageStream = q.dequeue.evalMap(fromClient => {
          ref.modify(currentState => {
            (State(currentState.messageCount + 1), ToClient(s"(${currentState.messageCount}): ${fromClient.userName} - ${fromClient.message}"))
          })
        }).through(t.publish)

        val serverStream = ChatServer.stream[IO](q, t)
        val combinedStream = Stream(messageStream, serverStream).parJoinUnbounded
        Server.createServer.use(_ =>
          combinedStream.compile.drain.as(ExitCode.Success))
      }
    ) yield exitCode
  }
}
