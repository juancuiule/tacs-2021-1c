package com.utn.tacs

import cats.Id
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, Timer}
import com.utn.tacs.db.{DatabaseConfig, DatabaseConnectionsConfig}
import com.utn.tacs.domain.`match`.{MatchService, MatchValidation, OutputMessage}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.cards._
import com.utn.tacs.domain.deck.DeckService
import com.utn.tacs.domain.user.{UserService, UserValidation}
import com.utn.tacs.infrastructure.endpoint._
import com.utn.tacs.infrastructure.repository.doobie.{DoobieCardRepository, DoobieUserRepository}
import com.utn.tacs.infrastructure.repository.memory._
import doobie.util.ExecutionContexts
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

    val dbConfig = DatabaseConfig(
      "jdbc:postgresql://localhost:5432/superamigos",
      "org.postgresql.Driver",
      "tacs",
      "secret123",
      DatabaseConnectionsConfig(32)
    )

    for {
      clientEc <- fs2.Stream.resource(ExecutionContexts.cachedThreadPool[F])
      client <- BlazeClientBuilder[F](clientEc).stream.map(FollowRedirect(3)(_))

      connEc <- fs2.Stream.resource(ExecutionContexts.fixedThreadPool[F](32))
      txnEc <- fs2.Stream.resource(ExecutionContexts.cachedThreadPool[F])
      xa <- fs2.Stream.resource(DatabaseConfig.dbTransactor(
        dbConfig,
        connEc,
        Blocker.liftExecutionContext(txnEc)
      ))

      matchRepo = MatchMemoryRepository()
      deckRepo = DeckMemoryRepository()
      authRepo = AuthMemoryRepository(key)

      userRepo = DoobieUserRepository[F](xa) // UserMemoryRepository()
      authenticator = Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)
      routeAuth = SecuredRequestHandler(authenticator)

      deckService = DeckService(deckRepo)
      deckEndpoints = DeckEndpoints[F, HMACSHA256](
        repository = deckRepo,
        deckService,
        auth = routeAuth
      )

      userService = UserService(userRepo, validation = UserValidation(userRepo))
      userEndpoints = UserEndpoints.endpoints[F, BCrypt, HMACSHA256](
        userService,
        cryptService = BCrypt.syncPasswordHasher[F],
        auth = routeAuth
      )

      //      val cardRepo = CardMemoryRepository()
      cardRepo = DoobieCardRepository[F](xa)
      cardService = CardService(cardRepo, validation = CardValidation(cardRepo))

      matchService = MatchService(matchRepo, validation = MatchValidation(matchRepo), deckService, cardService)
      matchEndpoints = MatchEndpoints[F, HMACSHA256](
        service = matchService,
        userService,
        deckService,
        auth = routeAuth
      )

      matchRoomEndpoints = MatchRoomEndpoints[F, HMACSHA256](
        service = matchService,
        auth = routeAuth,
        topic = topic
      )

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

      serverEc <- fs2.Stream.resource(ExecutionContexts.cachedThreadPool[F])
      exitCode <- BlazeServerBuilder[F](serverEc)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(Logger.httpApp(logHeaders = true, logBody = false)(httpApp))
        .serve
    } yield exitCode
  }
}
