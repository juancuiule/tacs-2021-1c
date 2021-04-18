package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl


object DeckEndpoints {
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
