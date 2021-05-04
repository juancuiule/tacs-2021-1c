package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.`match`.{Match, MatchService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}


class MatchEndpoints[F[+_] : Sync](matchServiceImpl: MatchService[F]) extends Http4sDsl[F] {

  implicit def decoder[T[_] : Sync]: EntityDecoder[T, Match] = jsonOf[T, Match]

  def endpoints(): HttpRoutes[F] = {
    //val dsl = new Http4sDsl[F] {}
    //import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / matchId =>
        matchServiceImpl.getMatch(matchId)
          .map(m => Ok(m.asJson))
          .getOrElse(NotFound(s"match:${matchId} not found"))
      case req@POST -> Root =>
        for {
          payload <- req.as[Map[String, String]]
          newMatch <- matchServiceImpl.createMatch(payload("player1_id"), payload("player2_id"), payload("deck_id"))
          resp <- Created(newMatch.asJson)
        } yield resp
      case req@PUT -> Root / matchId / "withdraw" =>
        for {
          payload <- req.as[Map[String, String]]
          loserPlayer = payload("loser_player_id")
          withDrawResult = matchServiceImpl.withdraw(matchId, loserPlayer)
          resp <- withDrawResult match {
            case Left(_) => NotFound()
            case Right(m) => Accepted(m.asJson)
          }
        } yield resp
      case GET -> Root / matchId / "replay" =>
        Ok(matchServiceImpl.getPlayedRounds(matchId)
          .map(r => r.asJson))
    }
  }
}

object MatchEndpoints {
  def apply[F[+_] : Sync](service: MatchService[F]): HttpRoutes[F] =
    new MatchEndpoints[F](service).endpoints()
}