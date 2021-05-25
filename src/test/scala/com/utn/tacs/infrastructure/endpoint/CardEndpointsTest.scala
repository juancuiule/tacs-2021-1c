//package com.utn.tacs
//package infrastructure.endpoint
//
//import cats.effect._
//import com.utn.tacs.domain.cards.{CardService, CardValidation, SuperheroAPIService}
//import com.utn.tacs.domain.deck.Deck
//import com.utn.tacs.infrastructure.repository.memory.{CardMemoryRepository, UserMemoryRepository}
//import io.circe._
//import io.circe.generic.semiauto._
//import org.http4s._
//import org.http4s.circe._
//import org.http4s.client.Client
//import org.http4s.client.blaze.BlazeClientBuilder
//import org.http4s.client.dsl.Http4sClientDsl
//import org.http4s.client.middleware.FollowRedirect
//import org.http4s.dsl._
//import org.http4s.implicits._
//import org.http4s.server.Router
//import org.scalatest.funsuite.AnyFunSuite
//import org.scalatest.matchers.should.Matchers
//import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
//import tsec.mac.jca.HMACSHA256
//
//import scala.concurrent.ExecutionContext.global
//
//class CardEndpointsTest
//  extends AnyFunSuite
//    with Matchers
//    with ScalaCheckPropertyChecks
//    with SuperAmigosArbitraries
//    with Http4sDsl[IO]
//    with Http4sClientDsl[IO] {
//
//  implicit val deckEncoder: Encoder[Deck] = deriveEncoder
//  implicit val deckEnc: EntityEncoder[IO, Deck] = jsonEncoderOf
//  implicit val deckDecoder: Decoder[Deck] = deriveDecoder
//  implicit val deckDec: EntityDecoder[IO, Deck] = jsonOf
//
//  def getTestResources(implicit c: ContextShift[IO]): (AuthTest[IO], HttpApp[IO]) = {
//    val userRepo = UserMemoryRepository[IO]()
//    val auth = new AuthTest[IO](userRepo)
//    val cardsRepo = CardMemoryRepository()
//    val cardsValidation = CardValidation[IO](cardsRepo)
//    val cardsService = CardService[IO](cardsRepo, cardsValidation)
//  }
//
//  //  test("create and get deck") {
//  //    val (auth, deckRoutes) = getTestResources()
//  //
//  //    forAll { (deck: Deck, user: AdminUser) =>
//  //      (for {
//  //        createRq <- POST(deck, uri"/decks")
//  //        createRqAuth <- auth.embedToken(user.value, createRq)
//  //        createResp <- deckRoutes.run(createRqAuth)
//  //        deckResp <- createResp.as[Deck]
//  //        getDeckRq <- GET(Uri.unsafeFromString(s"/decks/${deckResp.id}"))
//  //        getDeckRqAuth <- auth.embedToken(user.value, getDeckRq)
//  //        getDeckResp <- deckRoutes.run(getDeckRqAuth)
//  //        deckResp2 <- getDeckResp.as[Deck]
//  //      } yield {
//  //        deckResp.name shouldBe deck.name
//  //        getDeckResp.status shouldEqual Ok
//  //        deckResp2.name shouldBe deck.name
//  //      }).unsafeRunSync()
//  //    }
//  //  }
//  //
//  //  test("user roles deleting decks") {
//  //    val (auth, deckRoutes) = getTestResources()
//  //
//  //    forAll { (user: PlayerUser, adminUser: AdminUser, deck: Deck) =>
//  //      (for {
//  //        createRq <- POST(deck, uri"/decks")
//  //        createRqAuth <- auth.embedToken(adminUser.value, createRq)
//  //        createResp <- deckRoutes.run(createRqAuth)
//  //        deckResp <- createResp.as[Deck]
//  //        deleteRq <- DELETE(Uri.unsafeFromString(s"/decks/${deckResp.id}"))
//  //          .flatMap(auth.embedToken(user.value, _))
//  //        deleteResp <- deckRoutes.run(deleteRq)
//  //      } yield deleteResp.status shouldEqual Unauthorized).unsafeRunSync()
//  //    }
//  //
//  //    forAll { (user: AdminUser, deck: Deck) =>
//  //      (for {
//  //        createRq <- POST(deck, uri"/decks")
//  //        createRqAuth <- auth.embedToken(user.value, createRq)
//  //        createResp <- deckRoutes.run(createRqAuth)
//  //        deckResp <- createResp.as[Deck]
//  //        deleteRq <- DELETE(Uri.unsafeFromString(s"/decks/${deckResp.id}"))
//  //          .flatMap(auth.embedToken(user.value, _))
//  //        deleteResp <- deckRoutes.run(deleteRq)
//  //      } yield deleteResp.status shouldEqual Ok).unsafeRunSync()
//  //    }
//  //  }
//}