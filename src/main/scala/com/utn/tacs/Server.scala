package com.utn.tacs

import cats.Id
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, Timer}
import com.utn.tacs.domain.`match`.{MatchService, MatchValidation, OutputMessage}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.cards._
import com.utn.tacs.domain.deck.DeckService
import com.utn.tacs.domain.user.{UserService, UserValidation}
import com.utn.tacs.infrastructure.endpoint._
import com.utn.tacs.infrastructure.repository.memory._
import fs2.concurrent.Topic
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.FollowRedirect
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig, Logger}
import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt


object Server {
  def createServer[F[+_] : ContextShift : ConcurrentEffect : Timer](
    topic: Topic[F, OutputMessage]
  ): fs2.Stream[F, ExitCode] = {
    val corsConfig = CORSConfig(
      anyOrigin = false,
      allowCredentials = false,
      maxAge = 1.day.toSeconds,
      allowedHeaders = Some(Set("Content-Type", "Authorization", "*", "Upgrade", "Connection")),
      allowedOrigins = List("http://localhost:3000").contains(_)
    )

    val key = HMACSHA256.generateKey[Id]

    val userRepo = UserMemoryRepository()
    val cardRepo = CardMemoryRepository()
    val matchRepo = MatchMemoryRepository()
    val deckRepo = DeckMemoryRepository()
    val authRepo = AuthMemoryRepository(key)

    val authenticator = Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)

    val routeAuth = SecuredRequestHandler(authenticator)

    val deckService = DeckService(deckRepo)
    val deckEndpoints = DeckEndpoints[F, HMACSHA256](
      repository = deckRepo,
      deckService,
      auth = routeAuth
    )

    val userService = UserService(userRepo, validation = UserValidation(userRepo))
    val userEndpoints = UserEndpoints.endpoints[F, BCrypt, HMACSHA256](
      userService,
      cryptService = BCrypt.syncPasswordHasher[F],
      auth = routeAuth
    )

    val cardService = CardService(cardRepo, validation = CardValidation(cardRepo))

    val matchService = MatchService(matchRepo, validation = MatchValidation(matchRepo), deckService, cardService)
    val matchEndpoints = MatchEndpoints[F, HMACSHA256](
      service = matchService,
      userService,
      deckService,
      auth = routeAuth
    )

    val matchRoomEndpoints = MatchRoomEndpoints[F, HMACSHA256](
      service = matchService,
      auth = routeAuth,
      topic = topic
    )


    for {
      client <- BlazeClientBuilder[F](global).stream.map(FollowRedirect(3)(_))

      cardEndpoints = CardEndpoints[F, HMACSHA256](
        repository = cardRepo,
        cardService = cardService,
        superHeroeService = SuperheroAPIService(client),
        auth = routeAuth
      )

      superheroEndpoints = SuperheroEndpoints[F](
        service = SuperheroAPIService(client)
      )

      httpApp = Router(
        "/cards" -> CORS(cardEndpoints, corsConfig),
        "/superheros" -> CORS(superheroEndpoints, corsConfig),
        "/decks" -> CORS(deckEndpoints, corsConfig),
        "/users" -> CORS(userEndpoints, corsConfig),
        "/matches" -> CORS(matchEndpoints, corsConfig),
        "/rooms" -> matchRoomEndpoints
      ).orNotFound

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(Logger.httpApp(logHeaders = true, logBody = false)(httpApp))
        .serve
    } yield exitCode
  }
}
