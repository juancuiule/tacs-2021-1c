package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.auth.Auth
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl


object AuthEndpoints {
  def authRoutes[F[_] : Sync](A: Auth[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@POST -> Root / "login" =>
        for {
          user <- req.as[Auth.LoginData]
          data <- A.login(user)
          resp <- Ok(data)
        } yield resp
      case req@POST -> Root / "signup" =>
        for {
          user <- req.as[Auth.LoginData]
          resp <- Created(s"user ${user.username} created")
        } yield resp
      case POST -> Root / "logout" =>
        for {
          resp <- ResetContent()
        } yield resp
    }
  }

}
