package com.utn.tacs

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.{Queue, Topic}
import org.http4s.websocket.WebSocketFrame

case class State(messageCount: Int)


case class ToClient(message: String)

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      q <- Queue.unbounded[IO, WebSocketFrame];
      t <- Topic[IO, WebSocketFrame](WebSocketFrame.Text("==="))
//      ref <- Ref.of[IO, State](State(1))

      exitCode <- {
//        val messageStream: Stream[IO, Unit] = q.dequeue.evalMap {
//          case Text(str, _) => {
//            ref.modify(currState => (currState, Text(str)))
//          }
//        }.through(t.publish)

//        val combinedStream = Stream(messageStream, Server.createServer[IO](q, t)).parJoinUnbounded
        Server.createServer[IO](q, t).compile.drain.as(ExitCode.Success)
      }
    } yield exitCode
  }
}
