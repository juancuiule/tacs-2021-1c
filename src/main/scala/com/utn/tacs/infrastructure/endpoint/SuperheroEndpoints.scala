package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.cards._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class SuperheroEndpoints[F[+_] : Sync](service: SuperheroAPIService[F]) extends Http4sDsl[F] {
  def endpoints(): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / IntVar(id) =>
        service.getById(id).flatMap {
          case Some(superhero) => Ok(superhero.asJson)
          case None => NotFound(s"superhero with id: $id, not found")
        }

      case GET -> Root / "name" / searchName =>
        val actionResult = service.searchSuperheroByName(searchName)
        actionResult.flatMap {
          case Some(superheros) => Ok(Json.obj(
            ("superheros", superheros.asJson)
          ))
          case None => NotFound(s"SuperHeroes not found for name $searchName")
        }


    }
}

object SuperheroEndpoints {
  def apply[F[+_] : Sync](service: SuperheroAPIService[F]): HttpRoutes[F] = new SuperheroEndpoints[F](service).endpoints()
}