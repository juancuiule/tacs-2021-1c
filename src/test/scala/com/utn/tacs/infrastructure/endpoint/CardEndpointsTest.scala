package com.utn.tacs
package infrastructure.endpoint

import cats.data.OptionT
import cats.effect.{IO, Sync}
import com.utn.tacs.domain.cards._
import com.utn.tacs.infrastructure.repository.memory.{CardMemoryRepository, UserMemoryRepository}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, _}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

case class MockSuperHeroService[F[+_] : Sync]() extends SHService[F] {
  override def getById(id: Int): F[Option[Superhero]] = OptionT.fromOption(
    Some(
      Superhero(
        id,
        "Batman",
        Powerstats(intelligence = "81", strength = "40", speed = "29", durability = "55", power = "63", combat = "90"),
        SuperheroBiography("Batman", "DC Comics"),
        Appearance(("5'10", "178 cm"), ("170 lb", "77 kg")),
        Image("https://www.superherodb.com/pictures2/portraits/10/100/10441.jpg")
      )
    )
  ).value

  override def searchSuperheroByName(searchName: String): F[Option[List[Superhero]]] = OptionT.fromOption(Some(List[Superhero]())).value
}

case class AddCardFromHeroDTO(id: Int)

class CardEndpointsTest()
  extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with SuperAmigosArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  def getTestResources(): (AuthTest[IO], HttpApp[IO]) = {
    val userRepo = UserMemoryRepository[IO]()
    val auth = new AuthTest[IO](userRepo)
    val cardRepo = CardMemoryRepository[IO]()
    val cardValidation = CardValidation[IO](cardRepo)
    val cardService = CardService[IO](cardRepo, cardValidation)
    val superheroAPIService: SHService[IO] = MockSuperHeroService()
    val cardEndpoints = CardEndpoints(cardRepo, cardService, superheroAPIService, auth.securedRqHandler)
    val cardsRoutes = Router(("/cards", cardEndpoints)).orNotFound
    (auth, cardsRoutes)
  }

  case class GetCardsReponse(cards: List[Card])
  case class GetPublishersReponse(publishers: List[String])

  implicit val addCardDtoEncoder: Encoder[AddCardFromHeroDTO] = deriveEncoder
  implicit val addCardDtoEnc: EntityEncoder[IO, AddCardFromHeroDTO] = jsonEncoderOf[IO, AddCardFromHeroDTO]
  implicit val addCardDtoDecoder: Decoder[AddCardFromHeroDTO] = deriveDecoder
  implicit val addCardDtoDec: EntityDecoder[IO, AddCardFromHeroDTO] = jsonOf

  implicit val cardDecoder: EntityDecoder[IO, Card] = jsonOf[IO, Card]
  implicit val getCardsDecoder: EntityDecoder[IO, GetCardsReponse] = jsonOf[IO, GetCardsReponse]
  implicit val getPublisherDecoder: EntityDecoder[IO, GetPublishersReponse] = jsonOf[IO, GetPublishersReponse]

  test("create card") {
    val (auth, cardRoutes) = getTestResources()

    forAll { (user: AdminUser) =>
      (for {
        createRq <- POST(AddCardFromHeroDTO(69), uri"/cards")
        createRqAuth <- auth.embedToken(user.value, createRq)
        createResp <- cardRoutes.run(createRqAuth)
        card <- createResp.as[Card]
      } yield {
        createResp.status shouldBe Created
        card.name shouldBe "Batman"
      }).unsafeRunSync()
    }
  }

  test("create cards and get all") {
    val (auth, cardRoutes) = getTestResources()

    forAll { (user: AdminUser) =>
      (for {
        createRq1 <- POST(AddCardFromHeroDTO(69), uri"/cards")
        createRq1Auth <- auth.embedToken(user.value, createRq1)

        createRq2 <- POST(AddCardFromHeroDTO(70), uri"/cards")
        createRq2Auth <- auth.embedToken(user.value, createRq2)

        create1Resp <- cardRoutes.run(createRq1Auth)
        create2Resp <- cardRoutes.run(createRq2Auth)

        getCardsRq <- GET(uri"/cards")
        getCardsResp <- cardRoutes.run(getCardsRq)

        cardsResponse <- getCardsResp.as[GetCardsReponse]
      } yield {
        create1Resp.status shouldBe Created
        create2Resp.status shouldBe Created
        getCardsResp.status shouldBe Ok
        cardsResponse.cards should have size 2
      }).unsafeRunSync()
    }
  }

  test("create card and get publishers") {
    val (auth, cardRoutes) = getTestResources()

    forAll { (user: AdminUser) =>
      (for {
        createRq1 <- POST(AddCardFromHeroDTO(69), uri"/cards")
        createRq1Auth <- auth.embedToken(user.value, createRq1)
        create1Resp <- cardRoutes.run(createRq1Auth)

        getPublishersRq <- GET(uri"/cards/publishers")
        getCardsResp <- cardRoutes.run(getPublishersRq)

        publishersResp <- getCardsResp.as[GetPublishersReponse]
      } yield {
        create1Resp.status shouldBe Created
        getCardsResp.status shouldBe Ok
        publishersResp.publishers should contain ("DC Comics")
      }).unsafeRunSync()
    }
  }
}