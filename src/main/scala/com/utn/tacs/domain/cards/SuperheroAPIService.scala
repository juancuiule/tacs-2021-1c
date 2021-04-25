package com.utn.tacs.domain.cards

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{DecodeFailure, Response, Status, Uri}

sealed trait SuperheroApiError

case object SuperheroApiResponseParseError extends SuperheroApiError

case object SuperheroApiNotFoundError extends SuperheroApiError

case object SuperheroApiBadNameSearchError extends SuperheroApiError

case object ApiUnknownError extends SuperheroApiError

final case class SearchResponse(results: List[Card])

final case class ApiResponseError(response: String, error: String)

class SuperheroAPIService[F[+_] : Sync](C: Client[F]) {

  import CardsEncoding._

  val baseUri = uri"https://superheroapi.com/"
  val uriWithKey: Uri = baseUri.withPath("api/" + scala.util.Properties.envOrElse("SUPERHERO_API_KEY", "") + "/")

  private def handlingApiUnknownError(failureHandler: EitherT[F, DecodeFailure, SuperheroApiError]) = {
    failureHandler
      .leftMap(_ => SuperheroApiResponseParseError)
      .fold[Either[SuperheroApiError, Nothing]](Left(_), Left(_))
  }

  def getById(id: Int): F[Either[SuperheroApiError, Card]] = {
    C.get(uriWithKey / id.toString) {
      case r@Response(Status.Ok, _, _, _, _) =>
        r.attemptAs[Card].leftFlatMap[Card, SuperheroApiError](_ => EitherT {
          handlingApiUnknownError(r.attemptAs[ApiResponseError].map {
            case ApiResponseError("error", "invalid id") => SuperheroApiNotFoundError
            case _ => ApiUnknownError
          })
        }).value
      case _ => Either.left[SuperheroApiError, Card](ApiUnknownError).pure[F]
    }
  }

  def searchByName(searchName: String): F[Either[SuperheroApiError, SearchResponse]] = {
    C.get(uriWithKey / "search/" / searchName) {
      case r@Response(Status.Ok, _, _, _, _) => {
        r.attemptAs[SearchResponse].map(searchResponse => {
          searchResponse.copy(
            results = searchResponse.results.filter(card => card.stats.isDefined)
          )
        }).leftFlatMap[SearchResponse, SuperheroApiError](_ => EitherT {
          handlingApiUnknownError(r.attemptAs[ApiResponseError].map {
            case ApiResponseError("error", "character with given name not found") => SuperheroApiNotFoundError
            case ApiResponseError("error", "bad name search request") => SuperheroApiBadNameSearchError
            case _ => ApiUnknownError
          })
        })
      }.value
      case _ => Either.left[SuperheroApiError, SearchResponse](ApiUnknownError).pure[F]
    }
  }
}

object SuperheroAPIService {
  def apply[F[+_] : Sync](client: Client[F]) =
    new SuperheroAPIService[F](client)
}