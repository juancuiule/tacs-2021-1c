package com.utn.tacs

import cats.effect.IO
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.auth.Auth.UserDTO
import com.utn.tacs.infrastructure.endpoint.AuthEndpoints
import org.http4s.implicits._
import munit.CatsEffectSuite
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._

class AuthSpec extends CatsEffectSuite {

  private[this] val retLogin: IO[Response[IO]] = {
    val getHW = Request[IO](Method.POST, uri"/login").withEntity(UserDTO("test", "test").asJson)
    val authAlg = Auth.impl[IO]
    AuthEndpoints.authRoutes(authAlg).orNotFound(getHW)
  }

  test("Login returns status code 200") {
    assertIO(retLogin.map(_.status), Status.Ok)
  }

  test("Login returns username") {
    assertIO(retLogin.flatMap(_.as[String]), "{\"username\":\"test\",\"password\":\"test\"}")
  }
}