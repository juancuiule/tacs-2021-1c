package com.utn.tacs

import cats.effect.IO
import com.utn.tacs.Auth.LoginData
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite

class AuthSpec extends CatsEffectSuite {

  private[this] val retLogin: IO[Response[IO]] = {
    val getHW = Request[IO](Method.POST, uri"/login").withEntity(LoginData("test", "test"))
    val authAlg = Auth.impl[IO]
    Tacs1c2021Routes.authRoutes(authAlg).orNotFound(getHW)
  }

  test("Login returns status code 200") {
    assertIO(retLogin.map(_.status), Status.Ok)
  }

  test("Login returns username") {
    assertIO(retLogin.flatMap(_.as[String]), "{\"username\":\"test\",\"password\":\"test\"}")
  }
}