package com.utn.tacs.domain.cards


import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.{Decoder, HCursor}
import org.http4s.circe.{jsonOf, _}
import org.http4s.{EntityDecoder, MediaType}

case object CardsEncoding {
  val invalidStatsValues: List[String] = List("null", "0 kg", "0 cm", "-")

  implicit def cardEntityDecoder[F[_]](implicit S: Sync[F]): EntityDecoder[F, Card] =
    jsonOfWithMedia(MediaType.application.json)(S, cardDecoder)

  implicit def searchDecoder[F[_]](implicit S: Sync[F]): EntityDecoder[F, SearchResponse] = jsonOf[F, SearchResponse]

  implicit def apiResponseErrorDecoder[F[_]](implicit S: Sync[F]): EntityDecoder[F, ApiResponseError] = jsonOf[F, ApiResponseError]

  implicit val cardDecoder: Decoder[Card] = (c: HCursor) => for {
    id <- c.downField("id").as[Int]
    fullName <- c.downField("biography").downField("full-name").as[String]
    publisher <- c.downField("biography").downField("publisher").as[String]
    intelligence <- c.downField("powerstats").downField("intelligence").as[String]
    strength <- c.downField("powerstats").downField("strength").as[String]
    speed <- c.downField("powerstats").downField("speed").as[String]
    power <- c.downField("powerstats").downField("power").as[String]
    combat <- c.downField("powerstats").downField("combat").as[String]
    imageUrl <- c.downField("image").downField("url").as[String]
    name <- c.downField("name").as[String]
    height <- c.downField("appearance").downField("height").downN(1).as[String]
    weight <- c.downField("appearance").downField("weight").downN(1).as[String]
  } yield {
    val stats: Option[Stats] = if (
      List(intelligence, strength, speed, power, combat, height, weight).exists(invalidStatsValues.contains(_))
    ) None else Stats(
      height.replace(" cm", "").toInt,
      weight.replace(" kg", "").toInt,
      intelligence.toInt,
      speed.toInt,
      power.toInt,
      combat.toInt,
      strength.toInt).some
    val bio: Option[Biography] = if (List(fullName, publisher).exists(invalidStatsValues.contains(_))) None else Biography(fullName, publisher).some

    Card(id.some, name, stats, imageUrl, bio)
  }

}