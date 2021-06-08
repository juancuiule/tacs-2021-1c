package com.utn.tacs

import cats.effect.{ExitCode, IO, IOApp}
import com.utn.tacs.domain.`match`.{OutputMessage, SendToUsers}
import fs2.concurrent.Topic

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    scala.util.Properties.envOrNone("SUPERHERO_API_KEY") match {
      case Some(apiKey) if apiKey != "" =>
        println("Encontré la variable de entorno: SUPERHERO_API_KEY")
        for {
        topic <- Topic[IO, OutputMessage](SendToUsers(Set.empty, None))
        exitCode <- Server.createServer[IO](topic).compile.drain.as(ExitCode.Success)
      } yield exitCode
      case _ => {
        println("Falta la variable de entorno: SUPERHERO_API_KEY")
        IO(ExitCode.Error)
      }
    }
  }
}