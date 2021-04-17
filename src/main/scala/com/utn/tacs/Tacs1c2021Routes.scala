package com.utn.tacs

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object Tacs1c2021Routes {

  /*
  * /auth
  * POST /signup
  * POST /login
  * POST /logout <- auth
  *
  * /decks
  * POST /      <- admin
  * DELETE /:id <- admin
  * PATCH  /:id <- admin
  *
  * /match
  * POST /            <- crear partida
  * ??? /             <- continuar ???
  * GET /:id          <- traer partida
  * GET /:id/replay   <- traer jugadas y resultado de la partida
  * ??? /:id/withdraw <- abandonar
  *
  * /admin
  * GET /stats                  <- admin
  * GET /scoreboard?a=...&b=... <- admin
  * GET /stats/user/:id         <- admin
  *
  * /cards
  * GET ?search=...
  * GET /:id
  *
  *
  * */

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

    /*
    * POST /match               <- crear partida
    * GET  /match/:id/replay    <- traer jugadas y resultados de la partida
    * ???  /match/:id/withdraw  <-
    *
    * GET /admin/stats
    * GET /admin/scoreboard?a=...&b=...
    * GET /admin/stats/user/:id
    *
    * GET /cards/
    *
    * */
  }

  def decksRoutes[F[_] : Sync](): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case POST -> Root =>
        for {
          resp <- Created("deck created")
        } yield resp
      case GET -> Root =>
        for {
          resp <- Ok("decks")
        } yield resp
      case GET -> Root / id =>
        for {
          resp <- Ok(s"deck with id: ${id}")
        } yield resp
      case DELETE -> Root / id =>
        for {
          resp <- Ok(s"deleted deck with id: ${id}")
        } yield resp
      case PATCH -> Root / id =>
        for {
          resp <- Ok(s"updated deck with id: ${id}")
        } yield resp
    }
  }

}