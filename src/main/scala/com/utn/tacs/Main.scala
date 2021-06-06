package com.utn.tacs

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.{Queue, Topic}
import org.http4s.websocket.WebSocketFrame


object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      q <- Queue.unbounded[IO, WebSocketFrame];
      t <- Topic[IO, WebSocketFrame](WebSocketFrame.Text("==="))

      exitCode <- {
        Server.createServer[IO](q, t).compile.drain.as(ExitCode.Success)
      }
    } yield exitCode
  }
}
