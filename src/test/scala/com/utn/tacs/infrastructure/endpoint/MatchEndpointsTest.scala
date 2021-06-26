package com.utn.tacs.infrastructure.endpoint

import cats.effect.{IO, Sync}
import com.utn.tacs.SuperAmigosArbitraries
import com.utn.tacs.domain.`match`.{Match, MatchService, MatchValidation}
import com.utn.tacs.domain.cards.{CardService, CardValidation, SHService}
import com.utn.tacs.domain.deck.{Deck, DeckRepository, DeckService}
import com.utn.tacs.domain.user.{UserRepository, UserService, UserValidation}
import com.utn.tacs.infrastructure.repository.memory.{CardMemoryRepository, DeckMemoryRepository, MatchMemoryRepository, UserMemoryRepository}
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import org.http4s.circe.{jsonOf, _}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, _}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import tsec.mac.jca.HMACSHA256
//import io.circe.syntax._

class MatchEndpointsTest
  extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with SuperAmigosArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

//  implicit val matchEncoder: Encoder[Match] = deriveEncoder
//  implicit val matchEnc: EntityEncoder[IO, Match] = jsonEncoderOf
//  implicit val matchDecoder: Decoder[Match] = deriveDecoder
  implicit def decoder[T[_] : Sync]: EntityDecoder[T, Match] = jsonOf[T, Match]

  def getTestResources(): (AuthTest[IO], HttpApp[IO], UserRepository[IO], DeckRepository[IO]) = {
    val userRepo = UserMemoryRepository[IO]()
    val userValidation = UserValidation(userRepo)
    val userService = UserService(userRepo, userValidation)

    val auth = new AuthTest[IO](userRepo)

    val cardRepo = CardMemoryRepository[IO]()
    val cardValidation = CardValidation[IO](cardRepo)
    val cardService = CardService[IO](cardRepo, cardValidation)
    val superheroAPIService: SHService[IO] = MockSuperHeroService()
    val cardEndpoints = CardEndpoints(cardRepo, cardService, superheroAPIService, auth.securedRqHandler)
    val deckRepo = DeckMemoryRepository[IO]()
    val deckService = DeckService(deckRepo)
    val deckEndpoints =
      DeckEndpoints[IO, HMACSHA256](deckRepo, deckService, auth.securedRqHandler)

    val matchRepo = MatchMemoryRepository[IO]()
    val matchValidation = MatchValidation[IO](matchRepo)
    val matchService = MatchService(matchRepo, matchValidation, deckService, cardService)
    val matchEndpoints = MatchEndpoints[IO, HMACSHA256](matchService, userService, deckService, auth.securedRqHandler)

    val routes = Router(("/decks", deckEndpoints), ("/cards", cardEndpoints), ("/matches", matchEndpoints)).orNotFound
    (auth, routes, userRepo, deckRepo)
  }

  implicit val createMatchEncoder: Encoder[CreateMatchDTO] = deriveEncoder
  implicit val createMatchEnc: EntityEncoder[IO, CreateMatchDTO] = jsonEncoderOf[IO, CreateMatchDTO]
  implicit val createMatchDecoder: Decoder[CreateMatchDTO] = deriveDecoder
  implicit val createMatchDec: EntityDecoder[IO, CreateMatchDTO] = jsonOf
  case class CreateMatchDTO(player2: String, deckId: Int)

  test("create match") {
    val (auth, routes, userRepo, deckRepo) = getTestResources()

    forAll { (deck: Deck, player1: PlayerUser, player2: PlayerUser) =>
      (for {
        p1 <- userRepo.create(player1.value)
        p2 <- userRepo.create(player2.value)
        d1 <- deckRepo.create(deck)
        createRq <- POST(CreateMatchDTO(p2.userName, d1.id), uri"/matches")
        createRqAuth <- auth.embedToken(p1, createRq)
        createResp <- routes.run(createRqAuth)
        matchResp <- createResp.as[Match]
//        getMatchRq <- GET(Uri.unsafeFromString(s"/matches/${matchResp.matchId}"))
//        getMatchRqAuth <- auth.embedToken(p1, getMatchRq)
//        getMatchResp <- routes.run(getMatchRqAuth)
//        matchResp2 <- getMatchResp.as[Match]
      } yield {
        createResp.status shouldBe Created
        matchResp.player1 shouldBe p1.id.get
        matchResp.player2 shouldBe p2.id.get
      }).unsafeRunSync()
    }
  }
}