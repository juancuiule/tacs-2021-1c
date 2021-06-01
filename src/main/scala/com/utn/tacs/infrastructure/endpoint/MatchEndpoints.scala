package com.utn.tacs.infrastructure.endpoint

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import com.utn.tacs.domain.`match`.{Match, MatchAlreadyExistsError, MatchNotFoundError, MatchService}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.user.User
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo

class MatchEndpoints[F[+_] : Sync, Auth: JWTMacAlgo](
  service: MatchService[F],
  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
) extends Http4sDsl[F] {

  implicit def decoder[T[_] : Sync]: EntityDecoder[T, Match] = jsonOf[T, Match]

  private val getMatchByIdEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / matchId asAuthed _ => {
      val result: EitherT[F, MatchNotFoundError.type, Match] = service.getMatch(matchId)
      result.value.flatMap {
        case Left(MatchNotFoundError) => NotFound()
        case Right(m) => Ok(m.asJson)
      }
    }
  }

  implicit val createMatchDecoder: EntityDecoder[F, CreateMatchDTO] = jsonOf
  private val createMatchEndpoint: AuthEndpoint[F, Auth] = {
    case req@POST -> Root asAuthed _ =>
      val action: F[Either[MatchAlreadyExistsError, Match]] = for {
        post <- req.request.as[CreateMatchDTO]
        result <- service.createMatch(post.player1, post.player2, post.deckId).value
      } yield result
      action.flatMap {
        case Right(value) => Ok(value.asJson)
        case Left(MatchAlreadyExistsError(_)) => InternalServerError()
      }
  }

  implicit val withdrawMatchDecoder: EntityDecoder[F, WithdrawMatchDto] = jsonOf
  private val withdrawMatchEndpoint: AuthEndpoint[F, Auth] = {
    case req@PUT -> Root / matchId / "withdraw" asAuthed _ =>
      val action: F[Either[MatchNotFoundError.type, Match]] = for {
        payload <- req.request.as[WithdrawMatchDto]
        result <- service.withdraw(matchId, payload.loserPlayer).value
      } yield result
      action.flatMap {
        case Left(MatchNotFoundError) => NotFound()
        case Right(m) => Accepted(m.asJson)
      }
  }

  private val getMatchReplayEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / matchId / "replay" asAuthed _ =>
      service.getPlayedRounds(matchId) match {
        case Some(rounds) => Ok(Json.obj(("rounds", rounds.asJson)))
        case None => NotFound()
      }
  }

  def endpoints(): HttpRoutes[F] = {
    val authEndpoints = Auth.allRoles(
      getMatchByIdEndpoint
        .orElse(createMatchEndpoint)
        .orElse(withdrawMatchEndpoint)
        .orElse(getMatchReplayEndpoint)
    )
    auth.liftService(authEndpoints)
  }

  case class CreateMatchDTO(player1: String, player2: String, deckId: String)

  case class WithdrawMatchDto(loserPlayer: String)
}

object MatchEndpoints {
  def apply[F[+_] : Sync, Auth: JWTMacAlgo](
    service: MatchService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new MatchEndpoints[F, Auth](service, auth).endpoints()
}