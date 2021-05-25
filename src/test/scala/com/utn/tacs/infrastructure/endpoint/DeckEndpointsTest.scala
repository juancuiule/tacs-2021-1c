package com.utn.tacs
package infrastructure.endpoint

import cats.effect.IO
import com.utn.tacs.domain.cards.{Card, CardService, CardValidation, SHService}
import com.utn.tacs.domain.deck.{AddCardDTO, Deck, DeckService}
import com.utn.tacs.infrastructure.repository.memory.{CardMemoryRepository, DeckMemoryRepository, UserMemoryRepository}
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, _}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import tsec.mac.jca.HMACSHA256

class DeckEndpointsTest
  extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with SuperAmigosArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  implicit val deckEncoder: Encoder[Deck] = deriveEncoder
  implicit val deckEnc: EntityEncoder[IO, Deck] = jsonEncoderOf
  implicit val deckDecoder: Decoder[Deck] = deriveDecoder
  implicit val deckDec: EntityDecoder[IO, Deck] = jsonOf

  def getTestResources(): (AuthTest[IO], HttpApp[IO]) = {
    val userRepo = UserMemoryRepository[IO]()
    val auth = new AuthTest[IO](userRepo)

    val cardRepo = CardMemoryRepository()
    val cardValidation = CardValidation[IO](cardRepo)
    val cardService = CardService[IO](cardRepo, cardValidation)
    val superheroAPIService: SHService[IO] = MockSuperHeroService()
    val cardEndpoints = CardEndpoints(cardRepo, cardService, superheroAPIService, auth.securedRqHandler)
    val deckRepo = DeckMemoryRepository[IO]()
    val deckService = DeckService(deckRepo)
    val deckEndpoint =
      DeckEndpoints[IO, HMACSHA256](deckRepo, deckService, auth.securedRqHandler)
    val routes = Router(("/decks", deckEndpoint), ("/cards", cardEndpoints)).orNotFound
    (auth, routes)
  }

  implicit val addCardDtoEncoder: Encoder[AddCardFromHeroDTO] = deriveEncoder
  implicit val addCardDtoEnc: EntityEncoder[IO, AddCardFromHeroDTO] = jsonEncoderOf[IO, AddCardFromHeroDTO]
  implicit val addCardDtoDecoder: Decoder[AddCardFromHeroDTO] = deriveDecoder
  implicit val addCardDtoDec: EntityDecoder[IO, AddCardFromHeroDTO] = jsonOf

  implicit val addCardToDeckDtoEncoder: Encoder[AddCardDTO] = deriveEncoder
  implicit val addCardToDeckDtoEnc: EntityEncoder[IO, AddCardDTO] = jsonEncoderOf[IO, AddCardDTO]
  implicit val addCardToDeckDtoDecoder: Decoder[AddCardDTO] = deriveDecoder
  implicit val addCardToDeckDtoDec: EntityDecoder[IO, AddCardDTO] = jsonOf[IO, AddCardDTO]

  implicit val cardDecoder: EntityDecoder[IO, Card] = jsonOf[IO, Card]

  test("create and get deck") {
    val (auth, deckRoutes) = getTestResources()

    forAll { (deck: Deck, user: AdminUser) =>
      (for {
        createRq <- POST(deck, uri"/decks")
        createRqAuth <- auth.embedToken(user.value, createRq)
        createResp <- deckRoutes.run(createRqAuth)
        deckResp <- createResp.as[Deck]
        getDeckRq <- GET(Uri.unsafeFromString(s"/decks/${deckResp.id}"))
        getDeckRqAuth <- auth.embedToken(user.value, getDeckRq)
        getDeckResp <- deckRoutes.run(getDeckRqAuth)
        deckResp2 <- getDeckResp.as[Deck]
      } yield {
        deckResp.name shouldBe deck.name
        getDeckResp.status shouldEqual Ok
        deckResp2.name shouldBe deck.name
      }).unsafeRunSync()
    }
  }

  test("create card and add to deck") {
    val (auth, routes) = getTestResources()
    forAll { (user: AdminUser, deck: Deck) =>
      (for {
        createDeckRq <- POST(deck, uri"/decks")
        createDeckRqAuth <- auth.embedToken(user.value, createDeckRq)
        createDeckResp <- routes.run(createDeckRqAuth)
        deckResp <- createDeckResp.as[Deck]

        createCardRq <- POST(AddCardFromHeroDTO(69), uri"/cards")
        createCardRqAuth <- auth.embedToken(user.value, createCardRq)
        createCardResp <- routes.run(createCardRqAuth)
        card <- createCardResp.as[Card]

        addToDeckRq <- PATCH(AddCardDTO(card.id), Uri.unsafeFromString(s"/decks/${deckResp.id}"))
        addToDeckRqAuth <- auth.embedToken(user.value, addToDeckRq)
        addToDeckResp <- routes.run(addToDeckRqAuth)
        newDeck <- addToDeckResp.as[Deck]
      } yield {
        newDeck.cards should have size 1
      }).unsafeRunSync()
    }
  }

  test("user roles deleting decks") {
    val (auth, deckRoutes) = getTestResources()

    forAll { (user: PlayerUser, adminUser: AdminUser, deck: Deck) =>
      (for {
        createRq <- POST(deck, uri"/decks")
        createRqAuth <- auth.embedToken(adminUser.value, createRq)
        createResp <- deckRoutes.run(createRqAuth)
        deckResp <- createResp.as[Deck]
        deleteRq <- DELETE(Uri.unsafeFromString(s"/decks/${deckResp.id}"))
          .flatMap(auth.embedToken(user.value, _))
        deleteResp <- deckRoutes.run(deleteRq)
      } yield deleteResp.status shouldEqual Unauthorized).unsafeRunSync()
    }

    forAll { (user: AdminUser, deck: Deck) =>
      (for {
        createRq <- POST(deck, uri"/decks")
        createRqAuth <- auth.embedToken(user.value, createRq)
        createResp <- deckRoutes.run(createRqAuth)
        deckResp <- createResp.as[Deck]
        deleteRq <- DELETE(Uri.unsafeFromString(s"/decks/${deckResp.id}"))
          .flatMap(auth.embedToken(user.value, _))
        deleteResp <- deckRoutes.run(deleteRq)
      } yield deleteResp.status shouldEqual Ok).unsafeRunSync()
    }
  }
}