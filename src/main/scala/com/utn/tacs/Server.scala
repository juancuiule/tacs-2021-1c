package com.utn.tacs

import cats.effect.{ConcurrentEffect, Timer}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.infrastructure.endpoint.{AuthEndpoints, DeckEndpoints, HerosEndpoints}
import org.http4s.server.Router
//import cats.implicits._
import com.utn.tacs.domain.heros.Heros
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object Server {

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F]): Stream[F, Nothing] = {
    for {
      preClient <- BlazeClientBuilder[F](global).stream
      client = FollowRedirect(3)(preClient)

      authAlg = Auth.impl[F]
      herosAlg = Heros.impl[F](client)

      httpApp = Router(
        "/auth" -> AuthEndpoints.authRoutes[F](authAlg),
        "/decks" -> DeckEndpoints.decksRoutes(),
        "/superheros" -> HerosEndpoints.herosRoutes(herosAlg)
      ).orNotFound

      finalHttpApp = Logger.httpApp(true, false)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "127.0.0.1")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
