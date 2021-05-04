//package com.utn.tacs
//
//import cats.effect.IO
//import com.utn.tacs.domain.deck.{AddCardDTO, CreateDeckDTO, Deck, DeckService}
//import com.utn.tacs.infrastructure.endpoint.DeckEndpoints
//import com.utn.tacs.infrastructure.repository.memory.DeckMemoryRepository
//import io.circe.generic.auto._
//import io.circe.syntax._
//import munit.CatsEffectSuite
//import org.http4s._
//import org.http4s.circe._
//import org.http4s.client.dsl.Http4sClientDsl
//import org.http4s.dsl.Http4sDsl
//import org.http4s.implicits._
//import org.http4s.server.Router
//import tsec.mac.jca.HMACSHA256
//
//class DecksSpec extends CatsEffectSuite with Http4sDsl[IO] with Http4sClientDsl[IO] {
//  implicit val deckEncoder: EntityEncoder[IO, Deck] = jsonEncoderOf
//  implicit val deckDecoder: EntityDecoder[IO, Deck] = jsonOf
//
//  def getTestResources: (HttpApp[IO], DeckMemoryRepository[IO]) = {
//    val deckRepository = DeckMemoryRepository[IO]()
//    val deckService = DeckService[IO](deckRepository)
//    val deckEndpoints = DeckEndpoints[IO, HMACSHA256](deckRepository, deckService)
//    val deckRoutes = Router(("/decks", deckEndpoints)).orNotFound
//    (deckRoutes, deckRepository)
//  }
//
//  test("Create deck returns status code 201") {
//    val (routes, _) = getTestResources
//    (for {
//      request <- POST(CreateDeckDTO("testDeck").asJson, uri"/decks")
//      response <- routes.run(request)
//    } yield assertEquals(response.status, Created)).unsafeRunSync()
//  }
//
//  test("Create deck") {
//    val (routes, _) = getTestResources
//    (for {
//      request <- POST(CreateDeckDTO("testDeck").asJson, uri"/decks")
//      response <- routes.run(request)
//      createdDeck <- response.as[Deck]
//    } yield assertEquals(createdDeck.name, "testDeck")).unsafeRunSync()
//  }
//
//  test("Add card to deck") {
//    val (routes, repository) = getTestResources
//    (for {
//      createDeckRequest <- POST(CreateDeckDTO("testDeck").asJson, uri"/decks")
//      createDeckResponse <- routes.run(createDeckRequest)
//      createdDeck <- createDeckResponse.as[Deck]
//      request <- PATCH(AddCardDTO(1).asJson, uri"/decks/" / createdDeck.id.toString)
//      _ <- routes.run(request)
//      deck <- repository.get(createdDeck.id).value
//    } yield deck match {
//      case Some(d) => assert(d.cards.contains(1))
//      case _ => ()
//    }).unsafeRunSync()
//  }
//}