package com.utn.tacs.domain.cards

import cats.Applicative
import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.{EntityDecoder, Uri}
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._

import scala.collection.concurrent.TrieMap

class CardRepository[F[_] : Applicative] {
  private val cache = new TrieMap[String, Card]

  def get(id: String): F[Option[Card]] = cache.get(id).pure[F]

  def create(card: Card): F[Card] = {
    cache.put(card.id, card.copy(name = card.name ++ " cached"))
    card.pure[F]
  }
}

object CardRepository {
  def apply[F[_] : Applicative]() = new CardRepository[F]()
}

case object CardNotFoundError extends Product with Serializable


trait CardApiRequester[F[_]] {
//  def getByName(name: String): F[List[Card]]

  def getById(id: String): F[Card]
}

object CardApiRequester {
  val baseUri = uri"https://superheroapi.com/"
  val uriWithKey: Uri = baseUri.withPath("api/4157956970883904/")

  def apply[F[_]](implicit ev: CardApiRequester[F]): CardApiRequester[F] = ev

  def impl[F[_] : Sync](C: Client[F]): CardApiRequester[F] = new CardApiRequester[F] {
    implicit val decoder: EntityDecoder[F, Card] = jsonOf[F, Card]

    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._

    val cardRepo: CardRepository[F] = CardRepository()

    def getById(id: String): F[Card] = {
      val cacheCard = EitherT.fromOptionF(cardRepo.get(id), CardNotFoundError)
      cacheCard.value.flatMap {
        case Right(found) => found.pure[F]
        case Left(CardNotFoundError) =>
          for {
            card <- C.expect[Card](GET(uriWithKey / id)).adaptError({ case t => CardError(t) })
            _ <- cardRepo.create(card)
          } yield card
      }
    }

    // TODO:
    // Si buscas por name puede que haya mas de una carta que cumpla con el name buscado
    // por ejemplo name: batman deberia devolver las cartas:
    // id   |   name
    // 69   |   Batman
    // 70   |   Batman
    // 71   |   Batman II
//    def getByName(name: String): F[List[Card]] = ???


  }

  final case class CardError(e: Throwable) extends RuntimeException

}


