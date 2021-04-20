package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.cards.{Card, PowerStats} //, CardApiRequester}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object CardEndpoints {
  def cardsRoutes[F[_] : Sync](): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    val powerStats: PowerStats = PowerStats("10", "10", "10", "10", "10", "10")
    val card: Card = Card("1", "batman", powerStats)
    val cards: List[Card] = List(card, card, card)

    HttpRoutes.of[F] {
      case GET -> Root / id =>
        print(id)
        for {
          //          card <- cardRequester.getById(id)
          resp <- Ok(card.asJson)
        } yield resp
      case GET -> Root / "name" / name =>
        print(name)
        for {
          //          cards <- cardRequester.getByName(name)
          resp <- Ok(Json.obj(
            ("cards", cards.map(card => card.asJson).asJson)
          ))
        } yield resp
    }
  }
}
