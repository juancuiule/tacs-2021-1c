package com.utn.tacs

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Server.createServer.use(_ => IO.never).as(ExitCode.Success)
}
