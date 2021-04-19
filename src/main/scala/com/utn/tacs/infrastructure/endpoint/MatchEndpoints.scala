package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.`match`.MatchService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl


/*
  * /match
  * POST /            <- crear partida
  * ??? /             <- continuar ???
  * GET /:id          <- traer partida
  * GET /:id/replay   <- traer jugadas y resultado de la partida
  * ??? /:id/withdraw <- abandonar
 */


object MatchEndpoints {

  def decksRoutes[F[_] : Sync](): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / matchId =>
        val maybeMatch = MatchService.getMatch(matchId)
        maybeMatch
          .map(m => Ok(m.asJson))
          .getOrElse(NotFound(s"match:${matchId} not found"))
        /*val je : Match = new Match(matchId,"","","","","")
          Ok(je.asJson)
          */
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
