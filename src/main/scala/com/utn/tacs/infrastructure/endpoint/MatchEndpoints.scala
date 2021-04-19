package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.`match`.{Match, MatchService}
import com.utn.tacs.domain.auth.Auth
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.util.Try


/*
  * /match
  * POST /            <- crear partida
  * ??? /             <- continuar ???
  * GET /:id          <- traer partida
  * GET /:id/replay   <- traer jugadas y resultado de la partida
  * PUT /:id/withdraw <- abandonar
 */


object MatchEndpoints {

  def decksRoutes[F[_] : Sync](): HttpRoutes[F] = {
    implicit def decoder[F[_]: Sync]: EntityDecoder[F, Match] = jsonOf[F, Match]
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / matchId =>
        MatchService.getMatch(matchId)
          .map(m => Ok(m.asJson))
          .getOrElse(NotFound(s"match:${matchId} not found"))
      case req@POST -> Root =>
        for {
          payload  <- req.as[Match]
          newMatch <- MatchService.createMatch(payload.player1Id, payload.player2Id, payload.deckId)
          resp     <- Created(newMatch.asJson)
        } yield resp
      case req@PUT -> Root / matchId / "withdraw" =>
        val payload: Match = req[Match]
        MatchService.withdraw(matchId, payload.player1Id)
        Accepted(s"match: ${matchId} was withdrawn by player:${payload.player1Id}")
    }
  }
}
