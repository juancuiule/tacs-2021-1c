package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.cards.CardApiRequester
import com.utn.tacs.domain.cards.CardApiRequester.CardError
import io.circe.Json
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
        print(id)
        for {
          card <- cardRequester.getById(id.toInt)
          resp <- Ok(card.asJson)
        } yield resp

      case GET -> Root / "name" / name =>
        print(name)
        for {
          cards <- cardRequester.getByName(name)
          resp <- Ok(Json.obj(
            ("cards", cards.map(card => card.asJson).asJson)
          ))
        } yield resp

      //        FIXME: intento de get/id que devuelva respuestas mas controladas
      //         no logré hacerlo funcionar, no atrapa nada y devuelve un 500
      case GET -> Root / "test" / "1" =>
        try {
          for {
            card <- cardRequester.getById(5434354)
            resp <- Ok(card.asJson)
          } yield resp
        } catch {
          case _: CardError => NotFound("atrapa CardError")
          case _: Exception => NotFound("atrapa Exception")
        }


    }
  }
}
