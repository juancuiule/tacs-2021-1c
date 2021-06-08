package com.utn.tacs

import cats.effect.{ExitCode, IO, IOApp}
import com.utn.tacs.domain.`match`.{OutputMessage, SendToUsers}
import fs2.concurrent.Topic

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      topic <- Topic[IO, OutputMessage](SendToUsers(Set.empty, None))
      exitCode <- Server.createServer[IO](topic).compile.drain.as(ExitCode.Success)
    } yield exitCode
  }
}