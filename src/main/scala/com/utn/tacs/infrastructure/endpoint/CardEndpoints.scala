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

class CardEndpoints[F[+_] : Sync](repository: CardRepository[F], service: CardService[F]) extends Http4sDsl[F] {

  implicit val cardDecoder: EntityDecoder[F, Card] = jsonOf
  val getCardEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / IntVar(id) =>
        repository.get(id).flatMap {
          case Some(card) => Ok(card.asJson)
          case None => NotFound()
        }
    }

  def endpoints(): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / IntVar(id) =>
        val actionResult: EitherT[F, CardNotFoundError.type, Card] = service.get(id)
        actionResult.value.flatMap {
          case Left(CardNotFoundError) => NotFound()
          case Right(card) => Ok(card.asJson)
        }
      case req@POST -> Root =>
        val actionResult = for {
          card <- req.as[Card]
          result <- service.create(card).value
        } yield result

        actionResult.flatMap {
          case Right(card) => Ok(card.asJson)
          case Left(CardAlreadyExistsError(card)) => Conflict(card.asJson)
        }
      case GET -> Root :? PublisherQueryParamMatcher(publisher) => // +& PageSizeQueryParamMatcher(_) +& OffsetQueryParamMatcher(_) =>
        for {
          cards <- service.getByPublisher(publisher.getOrElse("")) // service.getAll(pageSize.getOrElse(100), offset.getOrElse(0))
          resp <- Ok(Json.obj(
            ("cards", cards.asJson)
          ))
        } yield resp
    }

  object PublisherQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("publisher")

  object PageSizeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("pagesize")

  object OffsetQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")

}

object CardEndpoints {
  def apply[F[+_] : Sync](repository: CardRepository[F],
                          service: CardService[F]): HttpRoutes[F] =
    new CardEndpoints[F](repository, service).endpoints()
}