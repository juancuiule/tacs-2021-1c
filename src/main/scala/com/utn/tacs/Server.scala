package com.utn.tacs

import cats.effect.{ConcurrentEffect, ContextShift, Resource, Timer}
import com.utn.tacs.domain.`match`.MatchService
import com.utn.tacs.domain.deck.DeckService
import org.http4s.server.middleware.{CORS, CORSConfig}

import scala.concurrent.duration.DurationInt
//import com.utn.tacs.domain.`match`.MatchService
//import com.utn.tacs.domain.`match`.MatchService
import com.utn.tacs.domain.`match`.MatchValidation
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.cards._
//import com.utn.tacs.domain.deck._
import com.utn.tacs.domain.user.{UserService, UserValidation}
import com.utn.tacs.infrastructure.endpoint._
import com.utn.tacs.infrastructure.repository.memory._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.{Router, Server => H4Server}
import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.ExecutionContext.global


object Server {

  def createServer[F[+_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, H4Server[F]] = {
    val corsConfig = CORSConfig(
      anyOrigin = false,
      allowCredentials = false,
      maxAge = 1.day.toSeconds,
      allowedOrigins = List("http://localhost:3000").contains(_)
    )

    val userRepo = UserMemoryRepository()
    val cardRepo = CardMemoryRepository()
    val matchRepo = MatchMemoryRepository()
    val deckRepo = DeckMemoryRepository()

    for {
      client <- BlazeClientBuilder[F](global).resource.map(FollowRedirect(3)(_))
      key <- Resource.liftF(HMACSHA256.generateKey[F])

      routeAuth = SecuredRequestHandler(
        authenticator = Auth.jwtAuthenticator[F, HMACSHA256](
          key,
          authRepo = AuthMemoryRepository(key),
          userRepo
        )
      )

      cardEndpoints = CardEndpoints[F, HMACSHA256](
        repository = cardRepo,
        cardService = CardService(cardRepo, validation = CardValidation(cardRepo)),
        superHeroeService = SuperheroAPIService(client),
        auth = routeAuth
      )

      superheroEndpoints = SuperheroEndpoints[F](
        service = SuperheroAPIService(client)
      )

      deckEndpoints = DeckEndpoints[F, HMACSHA256](
        repository = deckRepo,
        service = DeckService(deckRepo),
        auth = routeAuth
      )

      userEndpoints = UserEndpoints.endpoints[F, BCrypt, HMACSHA256](
        userService = UserService(userRepo, validation = UserValidation(userRepo)),
        cryptService = BCrypt.syncPasswordHasher[F],
        auth = routeAuth
      )

      matchEndpoints = MatchEndpoints[F, HMACSHA256](
        service = MatchService(matchRepo, validation = MatchValidation(matchRepo)),
        auth = routeAuth
      )

      httpApp = Router(
        "/cards" -> CORS(cardEndpoints, corsConfig),
        "/superheros" -> CORS(superheroEndpoints, corsConfig),
        "/decks" -> CORS(deckEndpoints, corsConfig),
        "/users" -> CORS(userEndpoints, corsConfig),
        "/matches" -> CORS(matchEndpoints, corsConfig)
      ).orNotFound

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(Logger.httpApp(logHeaders = true, logBody = false)(httpApp))
        .resource
    } yield exitCode
  }
}
