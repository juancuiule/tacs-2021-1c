package com.utn.tacs.domain.heros

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method._

trait Heros[F[_]] {
  def get(id: String): F[Heros.Hero]
}

object Heros {
  def apply[F[_]](implicit ev: Heros[F]): Heros[F] = ev

  def impl[F[_] : Sync](C: Client[F]): Heros[F] = new Heros[F] {
    val dsl = new Http4sClientDsl[F] {}

    import dsl._

    def get(id: String): F[Heros.Hero] = {
      C.expect[Hero](GET(uri"https://superheroapi.com/api/API_KEY/" / id))
        .adaptError { case t => HeroError(t) } // Prevent Client Json Decoding Failure Leaking
    }
  }

  final case class PowerStats(intelligence: String,
                              strength: String,
                              speed: String,
                              durability: String,
                              power: String,
                              combat: String
                             )

  final case class Hero(id: String, name: String, powerstats: PowerStats)

  final case class HeroError(e: Throwable) extends RuntimeException

  object Hero {
    implicit val heroDecoder: Decoder[Hero] = deriveDecoder[Hero]

    implicit def heroEntityDecoder[F[_] : Sync]: EntityDecoder[F, Hero] =
      jsonOf

    implicit val heroEncoder: Encoder[Hero] = deriveEncoder[Hero]

    implicit def heroEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Hero] =
      jsonEncoderOf
  }

  object PowerStats {
    implicit val heroDecoder: Decoder[PowerStats] = deriveDecoder[PowerStats]

    implicit def heroEntityDecoder[F[_] : Sync]: EntityDecoder[F, PowerStats] =
      jsonOf

    implicit val heroEncoder: Encoder[PowerStats] = deriveEncoder[PowerStats]

    implicit def heroEntityEncoder[F[_] : Applicative]: EntityEncoder[F, PowerStats] =
      jsonEncoderOf
  }

}