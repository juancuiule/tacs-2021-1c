package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.syntax.all._
import com.utn.tacs.domain.auth.Auth
import com.utn.tacs.domain.deck._
import com.utn.tacs.domain.user.User
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, _}
import tsec.jwt.algorithms.JWTMacAlgo


class DeckEndpoints[F[+_] : Sync, Auth: JWTMacAlgo](
  repository: DeckRepository[F],
  service: DeckService[F],
  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
) extends Http4sDsl[F] {

  implicit val deckDecoder: EntityDecoder[F, Deck] = jsonOf
  implicit val addCardDTODecoder: EntityDecoder[F, AddCardDTO] = jsonOf
  implicit val createCardDTODecoder: EntityDecoder[F, CreateDeckDTO] = jsonOf

  private val createDeckEndpoint: AuthEndpoint[F, Auth] = {
    case req@POST -> Root asAuthed _ =>
      val r = scala.util.Random
      for {
        post <- req.request.as[CreateDeckDTO]
        newDeck = Deck(id = r.nextInt(100), cards = Set(), name = post.name)
        c <- service.create(newDeck).value
        resp <- c match {
          case Left(_) => InternalServerError()
          case Right(value) => Created(value.asJson)
        }
      } yield resp
  }

  private val getDecksEndpoint: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      for {
        decks <- service.getAll(100, 0)
        resp <- Ok(Json.obj(("decks", decks.asJson)))
      } yield resp
  }

  private val getDeckByIdEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / IntVar(id) asAuthed _ =>
      for {
        deck <- service.get(id).value
        resp <- deck match {
          case Some(d) => Ok(d.asJson)
          case None => NotFound()
        }
      } yield resp
  }

  private val addCardToDeckEndpoint: AuthEndpoint[F, Auth] = {
    case req@PATCH -> Root / IntVar(id) asAuthed _ =>
      for {
        dto <- req.request.as[AddCardDTO]
        updated <- service.addCard(id, dto.cardId).value
        resp <- updated match {
          case Some(newDeck) => Ok(newDeck.asJson)
          case None => InternalServerError()
        }
      } yield resp
  }

  private val removeCardFromDeckEndpoint: AuthEndpoint[F, Auth] = {
    case DELETE -> Root / IntVar(id) / "card" / IntVar(cardId) asAuthed _ =>
      for {
        updated <- service.removeCard(id, cardId).value
        resp <- updated match {
          case Some(newDeck) => Ok(newDeck.asJson)
          case None => InternalServerError()
        }
      } yield resp
  }

  private val deleteDeckEndpoint: AuthEndpoint[F, Auth] = {
    case DELETE -> Root / IntVar(id) asAuthed _ => {
      for {
        deleted <- repository.delete(id).value
        resp <- deleted match {
          case Some(_) => Ok("Deleted")
          case None => NotFound()
        }
      } yield resp
    }
  }

  def endpoints(): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      val allRoles =
        getDeckByIdEndpoint
      val onlyAdmin =
        createDeckEndpoint
          .orElse(addCardToDeckEndpoint)
          .orElse(deleteDeckEndpoint)
          .orElse(removeCardFromDeckEndpoint)
      Auth.allRolesHandler(allRoles)(Auth.adminOnly(onlyAdmin))
    }
    getDecksEndpoint <+> auth.liftService(authEndpoints)
  }
}

object DeckEndpoints {
  def apply[F[+_] : Sync, Auth: JWTMacAlgo](
    repository: DeckRepository[F],
    service: DeckService[F],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new DeckEndpoints[F, Auth](repository, service, auth).endpoints()
}