package com.utn.tacs

import cats.effect.{ConcurrentEffect, Timer}
import com.utn.tacs.domain.cards._
import com.utn.tacs.infrastructure.endpoint.{CardEndpoints, SuperheroEndpoints}
import com.utn.tacs.infrastructure.repository.memory.CardMemoryRepository
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig, Logger}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._


object Server {

  def stream[F[+_] : ConcurrentEffect](implicit T: Timer[F]): Stream[F, Nothing] = {
    for {
      preClient <- BlazeClientBuilder[F](global).stream
      client = FollowRedirect(3)(preClient)

      cardRepo = CardMemoryRepository()
      cardValidator = CardValidation(cardRepo)
      cardService = CardService(cardRepo, cardValidator)
      superheroService = SuperheroAPIService(client)
      cardEndpoints = CardEndpoints[F](cardRepo, cardService, superheroService)

      corsConfig = CORSConfig(
        anyOrigin = false,
        allowCredentials = false,
        maxAge = 1.day.toSeconds,
        allowedOrigins = List("http://localhost:3000").contains(_)
      )


      superheroEndpoints = SuperheroEndpoints[F](superheroService)

      httpApp = Router(
        "/cards" -> CORS(cardEndpoints, corsConfig),
        "/superheros" -> CORS(superheroEndpoints, corsConfig)
      ).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = false)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
