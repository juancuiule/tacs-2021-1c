package com.utn.tacs.infrastructure.endpoint

import io.circe.Json
import cats.effect.Sync
import cats.implicits._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}


/*
  * /admin
  * GET /stats                  <- admin
  * GET /scoreboard?a=...&b=... <- admin
  * GET /stats/user/:id         <- admin
 */

object AdminEndpoints {
  implicit def decoder[F[_]: Sync]: EntityDecoder[F, Json] = jsonOf[F, Json]

  def routes[F[_] : Sync](): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "stats" =>
        for {
          resp <- Ok(
            Json.obj(
                ("total_matches", Json.fromInt(21)),
                ("total_matches_active",  Json.fromInt(2)),
                ("total_matches_finished",  Json.fromInt(16)),
                ("total_matches_canceled",  Json.fromInt(3))
            )
          )
        } yield resp
      case GET -> Root / "scoreboard" / "match" / id =>
        // cartas ganadas por cada uno, cartas que quedan en el mazo, carta en la mano, etc
        for {
          resp <- Ok(
            Json.obj(
                ("match_id", Json.fromString(id)),
                ("player_1", Json.obj(
                  ("id", Json.fromInt(1)),
                  ("won_cards", Json.fromInt(10)),
                  ("current_card_id", Json.fromInt(1))
                )),
                ("player_2", Json.obj(
                  ("id", Json.fromInt(2)),
                  ("won_cards", Json.fromInt(8)),
                  ("current_card_id", Json.fromInt(5))
                )),
                ("remaining_cards",  Json.fromInt(16)),
            )
          )
        } yield resp
      case GET -> Root / "stats" / "users" / id =>
        for {
          resp <- Ok(
            Json.obj(
                ("player_id", Json.fromString(id)),
                ("total_matches", Json.fromInt(21)),
                ("total_matches_active",  Json.fromInt(2)),
                ("total_matches_finished",  Json.fromInt(16)),
                ("total_matches_canceled",  Json.fromInt(3))
            )
          )
        } yield resp
    }
  }
}
