package com.utn.tacs.domain.cards

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.{Decoder, HCursor}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf


case object CardsEncoding {
  implicit def cardDecoder[F[_]](implicit S: Sync[F]): EntityDecoder[F, Card] = jsonOf[F, Card]

  implicit def searchDecoder[F[_]](implicit S: Sync[F]): EntityDecoder[F, SearchResponse] = jsonOf[F, SearchResponse]

  implicit def apiResponseErrorDecoder[F[_]](implicit S: Sync[F]): EntityDecoder[F, ApiResponseError] = jsonOf[F, ApiResponseError]

  implicit val biographyDecoder: Decoder[Biography] = (c: HCursor) => for {
    fullName <- c.downField("full-name").as[String]
    publisher <- c.downField("publisher").as[String]
  } yield Biography(fullName, publisher)

  implicit val powerStatsDecoder: Decoder[Option[PowerStats]] = (c: HCursor) => for {
    intelligence <- c.downField("intelligence").as[String]
    strength <- c.downField("strength").as[String]
    speed <- c.downField("speed").as[String]
    durability <- c.downField("durability").as[String]
    power <- c.downField("power").as[String]
    combat <- c.downField("combat").as[String]
  } yield {
    if (List(intelligence, strength, speed, durability, power, combat).contains("null")) {
      None
    } else {
      PowerStats(intelligence.toInt, strength.toInt, speed.toInt, durability.toInt, power.toInt, combat.toInt).some
    }
  }
}