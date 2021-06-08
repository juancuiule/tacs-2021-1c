package com.utn.tacs.infrastructure.endpoint

import cats.data.EitherT
import cats.effect.{Concurrent, Sync, Timer}
import cats.syntax.all._
import com.utn.tacs.domain.`match`.{Match, MatchAlreadyExistsError, MatchNotFoundError, MatchService}
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.deck.DeckService
import com.utn.tacs.domain.user.{User, UserNotFoundError, UserService}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo

import scala.util.Right

class MatchEndpoints[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
  service: MatchService[F],
  userService: UserService[F],
  deckService: DeckService[F],
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

  private val getUserMatches: AuthEndpoint[F, Auth] = {
    case req@GET -> Root asAuthed _ =>
      val player = req.identity
      val matches = service.getMatchesForPlayer(player.id.get)
      Ok(Json.obj(("matches", matches.asJson)))
  }

  private val createMatchEndpoint: AuthEndpoint[F, Auth] = {
    // TODO: validar que no este jugando partida contra él mismo
    case req@POST -> Root asAuthed _ =>
      val action: F[Either[MatchAlreadyExistsError.type, Match]] = for {
        post <- req.request.as[CreateMatchDTO]
        player1 = req.identity
        player2 <- userService.getUserByName(post.player2).value
        deck <- deckService.get(post.deckId).value
        result <- (player2, deck) match {
          case (Right(user2), Some(_deck)) =>
            if (user2.id.contains(player1.id.get))
              ??? // player1 == player2 no se puede jugar
            else
              service.createMatch(player1.id.get, user2.id.get, _deck.id).value
          case (Right(_), None) => ??? // no existe ese deck
          case (Left(UserNotFoundError), _) => ??? // no existe este otro usuario
          case _ => Left(MatchAlreadyExistsError).pure[F]
        }
      } yield result
      action.flatMap {
        case Right(value) => Ok(value.asJson)
        case Left(MatchAlreadyExistsError) => InternalServerError()
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
        .orElse(getMatchReplayEndpoint)
        .orElse(getUserMatches)
    )
    auth.liftService(authEndpoints)
  }

  implicit val createMatchDecoder: EntityDecoder[F, CreateMatchDTO] = jsonOf

  case class CreateMatchDTO(player2: String, deckId: Int)

  object TokenQueryParamMatcher extends QueryParamDecoderMatcher[String]("access-token")
}

object MatchEndpoints {
  def apply[F[+_] : Sync : Concurrent : Timer, Auth: JWTMacAlgo](
    service: MatchService[F],
    userService: UserService[F],
    deckService: DeckService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new MatchEndpoints[F, Auth](service, userService, deckService, auth).endpoints()
}