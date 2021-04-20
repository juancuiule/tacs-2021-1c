package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import io.circe.Json
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object DeckEndpoints {
  def decksRoutes[F[_] : Sync](): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    val jsonCard = Json.obj(
      ("id", Json.fromString("1")),
      ("name", Json.fromString("Batman")),
      ("powerstats", Json.obj())
    )

    val jsonDeck = (id: String) => Json.obj(
      ("id", Json.fromString(id)),
      ("name", Json.fromString("deckDc")),
      ("cards", List(jsonCard, jsonCard, jsonCard).asJson)
    )

    HttpRoutes.of[F] {
      case POST -> Root =>
        for {
          resp <- Created(jsonDeck("1"))
        } yield resp
      case GET -> Root =>
        for {
          resp <- Ok(Json.obj(("decks", List(jsonDeck("1"), jsonDeck("2")).asJson)))
        } yield resp
      case GET -> Root / id =>
        for {
          resp <- Ok(jsonDeck(id))
        } yield resp
      case DELETE -> Root / id =>
        for {
          resp <- Ok(s"deleted deck with id: ${id}")
        } yield resp
      case PATCH -> Root / id =>
        for {
          resp <- Ok(jsonDeck(id))
        } yield resp
    }
  }
}
