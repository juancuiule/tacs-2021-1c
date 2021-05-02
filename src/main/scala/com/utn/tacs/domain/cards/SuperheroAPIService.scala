package com.utn.tacs.domain.cards

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Response, Status, Uri}

sealed trait SuperheroApiError

case object SuperheroApiResponseParseError extends SuperheroApiError

case object SuperheroApiNotFoundError extends SuperheroApiError

case object SuperheroApiBadNameSearchError extends SuperheroApiError

case object ApiUnknownError extends SuperheroApiError

final case class SearchResponse(results: List[Card])

final case class SuperheroSearchResponse(results: List[Superhero])

final case class ApiResponseError(response: String, error: String)

class SuperheroAPIService[F[+_] : Sync](C: Client[F]) {

  val baseUri = uri"https://superheroapi.com/"
  val uriWithKey: Uri = baseUri.withPath("api/" + scala.util.Properties.envOrElse("SUPERHERO_API_KEY", "") + "/")

  implicit val heroeDecoder: EntityDecoder[F, Superhero] = jsonOf[F, Superhero]
  implicit val searchDecder: EntityDecoder[F, SuperheroSearchResponse] = jsonOf[F, SuperheroSearchResponse]

  def getById(id: Int): F[Option[Superhero]] = {
    C.get(uriWithKey / id.toString) {
      case r@Response(Status.Ok, _, _, _, _) => r.attemptAs[Superhero].fold(_ => None, Some(_))
      case _ => None.pure[F]
    }
  }

  def searchSuperheroByName(searchName: String): F[Option[List[Superhero]]] = {
    C.get(uriWithKey / "search/" / searchName) {
      case r@Response(Status.Ok, _, _, _, _) =>
        r.attemptAs[SuperheroSearchResponse].fold(_ => None, _.results.some)
      case _ => None.pure[F]
    }
  }
}

object SuperheroAPIService {
  def apply[F[+_] : Sync](client: Client[F]) =
    new SuperheroAPIService[F](client)
}