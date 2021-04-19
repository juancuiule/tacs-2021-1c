package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.cards.CardApiRequester
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object CardEndpoints {
  def cardsRoutes[F[_] : Sync](cardRequester: CardApiRequester[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / id =>
        for {
          card <- cardRequester.getById(id)
          resp <- Ok(card.asJson)
        } yield resp
    }
  }
}
