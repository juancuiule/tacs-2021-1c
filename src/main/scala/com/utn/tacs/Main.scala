package com.utn.tacs

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Sync}
import fs2.Stream
import fs2.concurrent.{Queue, Topic}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

case class State(messageCount: Int)

case class FromClient(userName: String, message: String)

case class ToClient(message: String)

object Algo {
  def chatRoutes[F[_] : Sync : Concurrent](
    q: Queue[F, FromClient],
    t: Topic[F, ToClient]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / userName =>
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

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for (
      q <- Queue.unbounded[IO, FromClient];
      t: Topic[IO, ToClient] <- Topic[IO, ToClient](ToClient("==="));
      ref <- Ref.of[IO, State](State(1));

      exitCode <- {
        val messageStream: Stream[IO, Unit] = q.dequeue.evalMap(fromClient => {
          ref.modify(currentState => {
            (State(currentState.messageCount + 1), ToClient(s"(${currentState.messageCount}): ${fromClient.userName} - ${fromClient.message}"))
          })
        }).through(t.publish)

        val combinedStream = Stream(messageStream, Server.createServer[IO](q, t)).parJoinUnbounded

        combinedStream.compile.drain.as(ExitCode.Success)
      }
    ) yield exitCode
  }
}
