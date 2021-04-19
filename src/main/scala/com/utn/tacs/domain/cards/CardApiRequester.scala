package com.utn.tacs.domain.cards

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._

trait CardApiRequester[F[_]] {

  def getById(id: String): F[Card]
}

object CardApiRequester {

  val cardsCache: List[Card] = List()

  def apply[F[_]](implicit ev: CardApiRequester[F]): CardApiRequester[F] = ev

  def impl[F[_] : Sync](C: Client[F]): CardApiRequester[F] = new CardApiRequester[F] {
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._

    def getById(id: String): F[Card] = {
      implicit val decoder: EntityDecoder[F, Card] = jsonOf[F, Card]
      C.expect[Card](GET(uri"https://superheroapi.com/api/API-KEY/" / id))
        .adaptError { case t => CardError(t) } // Prevent Client Json Decoding Failure Leaking
    }

    //TODO: Implementar con cache
    // estaria bueno que devuelva un Option[Card]
//    override def getWithCache(id: String): Option[Card] = cardsCache find (_.id == id) match {
//      case Some(card) => Some(card)
//      case None => getById(id)
//    }

  }

  final case class CardError(e: Throwable) extends RuntimeException

}


