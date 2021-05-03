package com.utn.tacs.infrastructure.endpoint

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.cards._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}


class CardEndpoints[F[+_] : Sync](repository: CardRepository, cardService: CardService[F], superHeroeService: SuperheroAPIService[F]) extends Http4sDsl[F] {
  case class AddCardDTO(id: Int)

  implicit val cardDecoder: EntityDecoder[F, Card] = jsonOf
  implicit val cardBla: EntityDecoder[F, AddCardDTO] = jsonOf

  val getCardEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / IntVar(id) =>
        repository.get(id).pure[F].flatMap {
          case Some(card) => Ok(card.asJson)
          case None => NotFound()
        }
    }

  def endpoints(): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / IntVar(id) =>
        val actionResult: EitherT[F, CardNotFoundError.type, Card] = cardService.get(id)
        actionResult.value.flatMap {
          case Left(CardNotFoundError) => NotFound()
          case Right(card) => Ok(card.asJson)
        }

      // TODO: esto es para admins
      case req@POST -> Root =>
        val actionResult = for {
          cardBlaBla <- req.as[AddCardDTO]
          maybeSuperhero <- superHeroeService.getById(cardBlaBla.id)
        } yield maybeSuperhero

        // TODO: mejores errores
        actionResult.flatMap {
          case None => BadRequest("The superheroe does not exist")
          case Some(superhero: Superhero) => superhero.card match {
            case Some(card) => Created(repository.create(card).asJson)
            case None => BadRequest("The superheroe can't convert to card")
          }
        }


      case GET -> Root :? PublisherQueryParamMatcher(publisher) => // +& PageSizeQueryParamMatcher(_) +& OffsetQueryParamMatcher(_) =>
        for {
          cards <- cardService.getByPublisher(publisher.getOrElse("")) // service.getAll(pageSize.getOrElse(100), offset.getOrElse(0))
          resp <- Ok(Json.obj(
            ("cards", cards.asJson)
          ))
        } yield resp
      case GET -> Root / "publishers" =>
        for {
          publishers <- cardService.getPublishers
          resp <- Ok(Json.obj(("publishers", publishers.asJson)))
        } yield resp
    }

  object PublisherQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("publisher")

  object PageSizeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("pagesize")

  object OffsetQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")

}


object CardEndpoints {
  def apply[F[+_] : Sync](repository: CardRepository,
                          cardService: CardService[F],
                          superHeroeService: SuperheroAPIService[F]): HttpRoutes[F] =
    new CardEndpoints[F](repository, cardService, superHeroeService).endpoints()
}