package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.deck.{Deck, DeckRepository, DeckService}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class DeckEndpoints[F[+_] : Sync](repository: DeckRepository[F], service: DeckService[F]) extends Http4sDsl[F] {

  implicit val deckDecoder: EntityDecoder[F, Deck] = jsonOf

  def endpoints(): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req@POST -> Root =>
        for {
          post <- req.as[Deck]
          c <- service.create(post)
          resp <- Created(c.asJson)
        } yield resp
      case GET -> Root =>
        for {
          decks <- repository.getAll
          resp <- Ok(Json.obj(("decks", decks.asJson)))
        } yield resp
      case GET -> Root / IntVar(id) =>
        for {
          optionDeck <- service.get(id)
          resp <- optionDeck match {
            case Some(deck) => Ok(deck.asJson)
            case None => NotFound()
          }
        } yield resp
    }


}

object DeckEndpoints {
  def apply[F[+_] : Sync](repository: DeckRepository[F],
                          service: DeckService[F]): HttpRoutes[F] =
    new DeckEndpoints[F](repository, service).endpoints()
}