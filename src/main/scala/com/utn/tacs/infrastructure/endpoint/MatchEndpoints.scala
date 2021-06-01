package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import com.utn.tacs.domain.`match`.{Match, MatchService}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.user.User
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo


class MatchEndpoints[F[+_] : Sync, Auth: JWTMacAlgo](
  service: MatchService[F],
  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
) extends Http4sDsl[F] {

  implicit def decoder[T[_] : Sync]: EntityDecoder[T, Match] = jsonOf[T, Match]

  def routes(): AuthEndpoint[F, Auth] = {
    //val dsl = new Http4sDsl[F] {}
    //import dsl._
    //    HttpRoutes.of[F] {
    case GET -> Root / matchId asAuthed _ => {
//      player1Id: String, player2Id: String, deckId: String
      service.createMatch("", "", "")
      Ok(matchId)
    }
//      service.getMatch(matchId)
//        .map(m => Ok(m.asJson))
//        .getOrElse(NotFound(s"match:${matchId} not found"))
//    case req@POST -> Root asAuthed _ =>
//      for {
//        payload <- req.request.as[Map[String, String]]
//        newMatch <- service.createMatch(payload("player1_id"), payload("player2_id"), payload("deck_id"))
//        resp <- Created(newMatch.asJson)
//      } yield resp
//    case req@PUT -> Root / matchId / "withdraw" asAuthed _ =>
//      for {
//        payload <- req.request.as[Map[String, String]]
//        loserPlayer = payload("loser_player_id")
//        withDrawResult = service.withdraw(matchId, loserPlayer)
//        resp <- withDrawResult match {
//          case Left(_) => NotFound()
//          case Right(m) => Accepted(m.asJson)
//        }
//      } yield resp
//    case GET -> Root / matchId / "replay" asAuthed _ =>
//      Ok(service.getPlayedRounds(matchId)
//        .map(r => r.asJson))
  }
  //  }

  def endpoints(): HttpRoutes[F] = {
    auth.liftService(Auth.allRoles(routes()))
  }
}

object MatchEndpoints {
  def apply[F[+_] : Sync, Auth: JWTMacAlgo](
    service: MatchService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new MatchEndpoints[F, Auth](service, auth).endpoints()
}