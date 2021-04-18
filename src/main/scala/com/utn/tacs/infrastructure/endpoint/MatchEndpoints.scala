package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.`match`.{Match, MatchService}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe.jsonEncoder

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
        var je : Match = new Match("","","","","","")
        je.
        maybeMatch
          .map(m => Ok(m.asJson))
          .getOrElse(Ok("no se encontro el matchId {matchId}"))
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
