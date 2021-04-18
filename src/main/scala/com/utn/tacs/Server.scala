package com.utn.tacs

import cats.effect.{ConcurrentEffect, Timer}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.infrastructure.endpoint.{AuthEndpoints, DeckEndpoints}
import org.http4s.server.Router
//import cats.implicits._
import fs2.Stream
//import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.concurrent.ExecutionContext.global

object Server {

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F]): Stream[F, Nothing] = {
    val authAlg = Auth.impl[F]

    val httpApp = Router(
      "/auth" -> AuthEndpoints.authRoutes[F](authAlg),
      "/decks" -> DeckEndpoints.decksRoutes(),
    ).orNotFound

    val finalHttpApp = Logger.httpApp(true, false)(httpApp)

    for {
      //      client <- BlazeClientBuilder[F](global).stream
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "127.0.0.1")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
