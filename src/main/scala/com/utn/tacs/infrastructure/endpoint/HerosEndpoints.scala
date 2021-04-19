package com.utn.tacs.infrastructure.endpoint

import com.utn.tacs.domain.heros.Heros

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._

object HerosEndpoints {
  def herosRoutes[F[_] : Sync](H: Heros[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / id => {
        for {
          hero <- H.get(id)
          resp <- Ok(hero.asJson)
        } yield resp
      }
    }
  }
}
