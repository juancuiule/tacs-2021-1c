package com.utn.tacs.infrastructure.endpoint

import com.utn.tacs.domain.cards.CardApiRequester

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._

object CardEndpoints {
  def cardsRoutes[F[_] : Sync](H: CardApiRequester[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / id => {
        for {
          card <- H.get(id)
          resp <- Ok(card.asJson)
        } yield resp
      }
    }
  }
}
