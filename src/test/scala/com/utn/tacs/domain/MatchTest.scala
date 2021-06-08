//package com.utn.tacs
//package domain
//
//import cats.effect.IO
//import com.utn.tacs.domain.`match`.{MatchService, MatchValidation}
//import com.utn.tacs.domain.cards.{Card, CardService, CardValidation, _}
//import com.utn.tacs.domain.deck.{Deck, DeckService}
//import com.utn.tacs.domain.user.{UserService, UserValidation}
//import com.utn.tacs.infrastructure.repository.memory.{CardMemoryRepository, DeckMemoryRepository, MatchMemoryRepository, UserMemoryRepository}
//import org.http4s.client.dsl.Http4sClientDsl
//import org.http4s.dsl.Http4sDsl
//import org.scalatest.funsuite.AnyFunSuite
//import org.scalatest.matchers.should.Matchers
//import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
//
//class MatchTest
//  extends AnyFunSuite
//    with Matchers
//    with ScalaCheckPropertyChecks
//    with SuperAmigosArbitraries
//    with Http4sDsl[IO]
//    with Http4sClientDsl[IO]{
//
//  def getTestResources: (CardService[IO], DeckService[IO], MatchService[IO], UserService[IO]) = {
//    val userRepo = UserMemoryRepository[IO]()
//    val userValidation = UserValidation[IO](userRepo)
//    val userService = UserService[IO](userRepo, userValidation)
//
//    val cardRepo = CardMemoryRepository()
//    val cardValidation = CardValidation[IO](cardRepo)
//    val cardService = CardService[IO](cardRepo, cardValidation)
//
//    val deckRepo = DeckMemoryRepository[IO]()
//    val deckService = DeckService(deckRepo)
//
//    val matchRepo = MatchMemoryRepository[IO]()
//    val matchValidation = MatchValidation[IO](matchRepo)
//    val matchService = MatchService[IO](matchRepo, matchValidation, deckService, cardService)
//
//    (cardService, deckService, matchService, userService)
//  }
//
//  test("create match with users and deck") {
//    val (
//      cardService,
//      deckService,
//      matchService,
//      _
//      ) = getTestResources
//
//    case object MError
//    forAll { () =>
//      (for {
//        _ <- cardService.create(
//          Card(1, "uno", Stats(10, 10, 10, 10, 10, 10, 10), "", Biography("", ""))
//        ).leftMap(_ => MError)
//        _ <- cardService.create(
//          Card(2, "dos", Stats(10, 10, 10, 10, 10, 10, 10), "", Biography("", ""))
//        ).leftMap(_ => MError)
//        _ <- cardService.create(
//          Card(3, "dos", Stats(10, 10, 10, 10, 10, 10, 10), "", Biography("", ""))
//        ).leftMap(_ => MError)
//        _ <- deckService.create(Deck(1, "Un deck", Set(1, 2, 3))).leftMap(_ => MError)
//        m <- matchService.createMatch(1, 2, 1)
//      } yield ({
//        m.player1 shouldBe 1
//        m.player2 shouldBe 2
//        m.currentState.cardsInDeck shouldBe List()
//      })).unsafeRunSync()
//    }
//  }
//}