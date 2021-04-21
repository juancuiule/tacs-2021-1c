package com.utn.tacs.domain.cards

import cats.Applicative
import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Uri}

import scala.collection.mutable.ListBuffer

class CardRepository[F[_] : Applicative] {

  // ListBuffer es mutable
  // se argumentar usar lista inmutable
  private val cache: ListBuffer[Card] = new ListBuffer[Card]()

  // TODO:
  //  es medio una cagada tener que envolver todos los objetos en F[_]
  //  estaria bueno que si pido una Card al repo, me duelva siempre una Card, no F[Card]

  def get(id: Int): F[Option[Card]] = {
    cache.find { aCard => aCard.id == id }
      .pure[F]
  }

  def getByName(name: String): F[List[Card]] = {
    cache.filter { aCard => aCard.name.toLowerCase contains name.toLowerCase }
      .toList
      .pure[F]
  }

  // : Unit
  def add(card: Card): F[Card] = {
    cache addOne card
    card.pure[F]
  }

  def addAll(cards: List[Card]): F[List[Card]] = {
    cache addAll cards
    cards.pure[F]
  }

}

object CardRepository {
  def apply[F[_] : Applicative]() = new CardRepository[F]()
}

case object CardNotFoundError extends Product with Serializable


trait CardApiRequester[F[_]] {
  def getByName(name: String): F[List[Card]]

  def getById(id: Int): F[Card]
}

object CardApiRequester {
  val baseUri = uri"https://superheroapi.com/"
  val uriWithKey: Uri = baseUri.withPath("api/" + scala.util.Properties.envOrElse("SUPERHERO_API_KEY", "" ) + "/")


  def apply[F[_]](implicit ev: CardApiRequester[F]): CardApiRequester[F] = ev

  def impl[F[_] : Sync](C: Client[F]): CardApiRequester[F] = new CardApiRequester[F] {
    implicit val cardDecoder: EntityDecoder[F, Card] = jsonOf[F, Card]
    implicit val searchDecoder: EntityDecoder[F, SearchResponse] = jsonOf[F, SearchResponse]

    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._

    val cardRepo: CardRepository[F] = CardRepository()

    def getById(id: Int): F[Card] = {
      val cacheCard = EitherT.fromOptionF(cardRepo.get(id), CardNotFoundError)
      cacheCard.value.flatMap {
        case Right(found) => found.pure[F]
        case Left(CardNotFoundError) =>
          for {
            card <- C.expect[Card](GET(uriWithKey / id.toString)).adaptError({ case t => CardError(t) })
            _ <- cardRepo.add(card)
          } yield card
      }
    }

    def getByName(name: String): F[List[Card]] = {
      // TODO: no se si tiene sentido cachear acá de está forma
      // porque una vez que ya hay un superheroe con ese nombre
      // guardado va a traer siempre el que este en caché y no
      // va a hacer la req a la API

      // TODO: falta manejar el error que devuelve la API si no
      // encuentra superheroe con ese nombre
      val cachedList = cardRepo.getByName(name)
      cachedList.flatMap {
        case Nil => C.expect[SearchResponse](GET(uriWithKey / "search/" / name)).adaptError({ case t => CardError(t) }).flatMap(cards => {
          cardRepo.addAll(cards.results)
        })
        case _ => cachedList
      }
    }
  }

  final case class SearchResponse(results: List[Card]) extends AnyVal

  final case class CardError(e: Throwable) extends RuntimeException

}


