package com.utn.tacs

import cats.effect.{ConcurrentEffect, Timer}
import com.utn.tacs.domain.`match`.MatchService
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.infrastructure.endpoint.{AdminEndpoints, AuthEndpoints, DeckEndpoints, CardEndpoints, MatchEndpoints}
import org.http4s.server.Router
//import cats.implicits._
import com.utn.tacs.domain.cards.CardApiRequester
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
      matchServiceImpl = MatchService.impl[F]
      cardApiRequester = CardApiRequester.impl[F](client)


      httpApp = Router(
        "/admin" -> AdminEndpoints.routes(),
        "/auth" -> AuthEndpoints.authRoutes[F](authAlg),
        "/decks" -> DeckEndpoints.decksRoutes(),
        "/matches" -> MatchEndpoints.matchRoutes(matchServiceImpl),
        "/cards" -> CardEndpoints.cardsRoutes(cardApiRequester)
      ).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = false)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "127.0.0.1")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
