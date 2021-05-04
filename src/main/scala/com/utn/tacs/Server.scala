package com.utn.tacs

import cats.effect.{ConcurrentEffect, ContextShift, Resource, Timer}
import com.utn.tacs.domain.`match`.MatchService
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.cards._
import com.utn.tacs.domain.deck._
import com.utn.tacs.domain.user.{UserService, UserValidation}
import com.utn.tacs.infrastructure.endpoint._
import com.utn.tacs.infrastructure.repository.memory.{AuthMemoryRepository, CardMemoryRepository, DeckMemoryRepository, UserMemoryRepository}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig, Logger}
import org.http4s.server.{Router, Server => H4Server}
import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._

object Server {

  def createServer[F[+_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, H4Server[F]] = {
    for {
      preClient <- BlazeClientBuilder[F](global).resource
      client = FollowRedirect(3)(preClient)

      cardRepo = CardMemoryRepository()
      cardValidator = CardValidation(cardRepo)
      cardService = CardService(cardRepo, cardValidator)
      superheroService = SuperheroAPIService(client)
      cardEndpoints = CardEndpoints[F](cardRepo, cardService, superheroService)
      matchEndpoints = MatchEndpoints[F](MatchService.impl)


      key <- Resource.liftF(HMACSHA256.generateKey[F])
      authRepo = AuthMemoryRepository(key)
      userRepo = UserMemoryRepository()
      userValidation = UserValidation(userRepo)
      userService = UserService(userRepo, userValidation)
      authenticator = Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)
      routeAuth = SecuredRequestHandler(authenticator)

      userEndpoints = UserEndpoints.endpoints[F, BCrypt, HMACSHA256](userService, BCrypt.syncPasswordHasher[F], routeAuth)

      deckRepo = DeckMemoryRepository()
      deckService = DeckService(deckRepo)
      deckEndpoints = DeckEndpoints[F, HMACSHA256](deckRepo, deckService, routeAuth)

      corsConfig = CORSConfig(
        anyOrigin = false,
        allowCredentials = false,
        maxAge = 1.day.toSeconds,
        allowedOrigins = List("http://localhost:3000").contains(_)
      )


      superheroEndpoints = SuperheroEndpoints[F](superheroService)

      httpApp = Router(
        "/cards" -> CORS(cardEndpoints, corsConfig),
        "/superheros" -> CORS(superheroEndpoints, corsConfig),
        "/decks" -> CORS(deckEndpoints, corsConfig),
        "/users" -> CORS(userEndpoints, corsConfig),
        "/matches" -> CORS(matchEndpoints, corsConfig)
      ).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = false)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "127.0.0.1")
        .withHttpApp(finalHttpApp)
        .resource
    } yield exitCode
  }
}
