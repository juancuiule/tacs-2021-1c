package com.utn.tacs
package infrastructure
package endpoint

import cats.effect._
import com.utn.tacs.domain.auth.SignupRequest
import com.utn.tacs.domain.user.{UserService, _}
import com.utn.tacs.infrastructure.repository.memory.UserMemoryRepository
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import tsec.authentication.{JWTAuthenticator, SecuredRequestHandler}
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.duration._

class UserEndpointsTest
  extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with SuperAmigosArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO]
    with LoginTest {

  def userRoutes(): HttpApp[IO] = {
    val userRepo = UserMemoryRepository[IO]()
    val userValidation = UserValidation[IO](userRepo)
    val userService = UserService[IO](userRepo, userValidation)
    val key = HMACSHA256.unsafeGenerateKey
    val jwtAuth = JWTAuthenticator.unbacked.inBearerToken(1.day, None, userRepo, key)
    val usersEndpoint = UserEndpoints.endpoints(
      userService,
      BCrypt.syncPasswordHasher[IO],
      SecuredRequestHandler(jwtAuth)
    )
    Router(("/users", usersEndpoint)).orNotFound
  }

  test("create user and log in") {
    val userEndpoint = userRoutes()
    forAll { userSignup: SignupRequest =>
      val (_, authorization) = signUpAndLogIn(userSignup, userEndpoint).unsafeRunSync()
      authorization shouldBe defined
    }
  }
}