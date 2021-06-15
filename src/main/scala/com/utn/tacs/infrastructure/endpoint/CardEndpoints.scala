package com.utn.tacs.infrastructure.endpoint

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.cards._
import com.utn.tacs.domain.user.User
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo


class CardEndpoints[F[+_] : Sync, Auth: JWTMacAlgo](
  repository: CardRepository[F],
  cardService: CardService[F],
  superHeroeService: SHService[F],
  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
) extends Http4sDsl[F] {
  private val addCardEndpoint: AuthEndpoint[F, Auth] = {
    case req@POST -> Root asAuthed _ =>
      val actionResult = for {
        addCardDTO <- req.request.as[AddCardDTO]
        maybeSuperhero <- superHeroeService.getById(addCardDTO.id)
      } yield maybeSuperhero

      // TODO: mejores errores
      actionResult.flatMap {
        case None => BadRequest("The superheroe does not exist")
        case Some(superhero: Superhero) => superhero.card match {
          case Some(card) => repository.create(card).flatMap(card => Created(card.asJson))
          case None => BadRequest("The superheroe can't convert to card")
        }
      }
  }

  implicit val addCardDTODecoder: EntityDecoder[F, AddCardDTO] = jsonOf

  implicit val cardDecoder: EntityDecoder[F, Card] = jsonOf
  private val getCardEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / IntVar(id) =>
        val actionResult: EitherT[F, CardNotFoundError.type, Card] = cardService.get(id)
        actionResult.value.flatMap {
          case Left(CardNotFoundError) => NotFound()
          case Right(card) => Ok(card.asJson)
        }
    }
  private val getCardsEndpoint: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root :? PublisherQueryParamMatcher(publisher) => // +& PageSizeQueryParamMatcher(_) +& OffsetQueryParamMatcher(_) =>
        for {
          cards <- publisher.fold(cardService.getAll())(pub => cardService.getByPublisher(pub))
          resp <- Ok(Json.obj(
            ("cards", cards.asJson)
          ))
        } yield resp
    }
  }
  private val getPublishersEndpoint: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "publishers" =>
        for {
          publishers <- cardService.getPublishers
          resp <- Ok(Json.obj(("publishers", publishers.asJson)))
        } yield resp
    }
  }

  def endpoints(): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      Auth.adminOnly(addCardEndpoint)
    }
    val unauthEndpoints = getCardEndpoint <+> getCardsEndpoint <+> getPublishersEndpoint
    unauthEndpoints <+> auth.liftService(authEndpoints)
  }

  case class AddCardDTO(id: Int)

  object PublisherQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("publisher")

  object PageSizeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("pagesize")

  object OffsetQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")

}


object CardEndpoints {
  def apply[F[+_] : Sync, Auth: JWTMacAlgo](
    repository: CardRepository[F],
    cardService: CardService[F],
    superHeroeService: SHService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new CardEndpoints[F, Auth](repository, cardService, superHeroeService, auth).endpoints()
}