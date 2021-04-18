package com.utn.tacs.domain.cards

import org.http4s.EntityDecoder
import io.circe.generic.auto._
import cats.effect.Sync
import cats.implicits._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._

trait CardApiRequester[F[_]] {
  def get(id: String): F[Card]
}

object CardApiRequester {
  def apply[F[_]](implicit ev: CardApiRequester[F]): CardApiRequester[F] = ev

  def impl[F[_] : Sync](C: Client[F]): CardApiRequester[F] = new CardApiRequester[F] {
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._

    def get(id: String): F[Card] = {
      implicit val decoder: EntityDecoder[F, Card] = jsonOf[F, Card]
      C.expect[Card](GET(uri"https://superheroapi.com/api/4157956970883904/" / id))
        .adaptError { case t => CardError(t) } // Prevent Client Json Decoding Failure Leaking
    }
  }

  final case class CardError(e: Throwable) extends RuntimeException

}


