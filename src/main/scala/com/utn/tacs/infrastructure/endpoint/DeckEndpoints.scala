package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.deck._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class DeckEndpoints[F[+_] : Sync](repository: DeckRepository[F], service: DeckService[F]) extends Http4sDsl[F] {

  implicit val deckDecoder: EntityDecoder[F, Deck] = jsonOf
  implicit val addCardDTODecoder: EntityDecoder[F, AddCardDTO] = jsonOf
  implicit val createCardDTODecoder: EntityDecoder[F, CreateDeckDTO] = jsonOf

  def endpoints(): HttpRoutes[F] =
    HttpRoutes.of[F] {
      // crear mazo
      case req@POST -> Root =>
        val r = scala.util.Random
        for {
          post <- req.as[CreateDeckDTO]
          newDeck = Deck(id = r.nextInt(100), cards = Set(), name = post.name)
          c <- service.create(newDeck)
          resp <- Created(c.asJson)
        } yield resp

      // traer mazos
      case GET -> Root =>
        for {
          decks <- service.getAll(100, 0)
          resp <- Ok(Json.obj(("decks", decks.asJson)))
        } yield resp

      // mazo por id
      case GET -> Root / IntVar(id) =>
        for {
          deck <- service.get(id).value
          resp <- deck match {
            case Some(d) => Ok(d.asJson)
            case None => NotFound()
          }
        } yield resp

      // agregar carta
      case req@PATCH -> Root / IntVar(id) =>
        for {
          dto <- req.as[AddCardDTO]
          updated <- service.addCard(id, dto.cardId).value
          resp <- updated match {
            case Some(newDeck) => Ok(newDeck.asJson)
            case None => InternalServerError()
          }
        } yield resp

      // borrar mazo
      case DELETE -> Root / IntVar(id) => {
        for {
          deleted <- repository.delete(id).value
          resp <- deleted match {
            case Some(_) => Ok("Deleted")
            case None => NotFound()
          }
        } yield resp
      }
    }

}

object DeckEndpoints {
  def apply[F[+_] : Sync](repository: DeckRepository[F],
                          service: DeckService[F]): HttpRoutes[F] =
    new DeckEndpoints[F](repository, service).endpoints()
}