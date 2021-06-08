package com.utn.tacs

import cats.effect.IO
import com.utn.tacs.domain.auth.SignupRequest
import com.utn.tacs.domain.cards.{Biography, Card, Stats}
import com.utn.tacs.domain.deck.Deck
import com.utn.tacs.domain.user.{Role, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import tsec.authentication.AugmentedJWT
import tsec.common.SecureRandomId
import tsec.jws.mac._
import tsec.jwt.JWTClaims
import tsec.mac.jca._

import java.time.Instant

trait SuperAmigosArbitraries {
  val userNameLength = 16
  val userNameGen: Gen[String] = Gen.listOfN(userNameLength, Gen.alphaChar).map(_.mkString)
  val deckNameGen: Gen[String] = Gen.listOfN(10, Gen.asciiChar).map(_.mkString)

  val cardUrlGen: Gen[String] = Gen.oneOf(
    "https://www.superherodb.com/pictures2/portraits/10/100/174.jpg",
    "https://www.superherodb.com/pictures2/portraits/10/100/1536.jpg",
    "https://www.superherodb.com/pictures2/portraits/10/100/957.jpg"
  )

  implicit val instant: Arbitrary[Instant] = Arbitrary[Instant] {
    for {
      millis <- Gen.posNum[Long]
    } yield Instant.ofEpochMilli(millis)
  }

  implicit val role: Arbitrary[Role] = Arbitrary[Role](Gen.oneOf(Role.values.toIndexedSeq))

  implicit val user: Arbitrary[User] = Arbitrary[User] {
    for {
      userName <- userNameGen
      password <- arbitrary[String]
      id <- Gen.option(Gen.posNum[Long])
      role <- arbitrary[Role]
    } yield User(userName, password, id, role)
  }

  implicit val card: Arbitrary[Card] = Arbitrary[Card] {
    for {
      id <- Gen.posNum[Int]
      name <- deckNameGen
      url <- cardUrlGen
      height <- Gen.posNum[Int]
      weight <- Gen.posNum[Int]
      intelligence <- Gen.posNum[Int]
      speed <- Gen.posNum[Int]
      power <- Gen.posNum[Int]
      combat <- Gen.posNum[Int]
      strength <- Gen.posNum[Int]
    } yield Card(id, name, stats = Stats(
      height,
      weight,
      intelligence,
      speed,
      power,
      combat,
      strength
    ), biography = Biography("a", "b"), image = url)
  }

  implicit val deck: Arbitrary[Deck] = Arbitrary[Deck] {
    for {
      deckName <- deckNameGen
      id <- Gen.option(Gen.posNum[Int])
    } yield Deck(id.getOrElse(1), deckName, Set())
  }

  case class AdminUser(value: User)

  case class PlayerUser(value: User)

  implicit val adminUser: Arbitrary[AdminUser] = Arbitrary {
    user.arbitrary.map(user => AdminUser(user.copy(role = Role.Admin)))
  }

  implicit val playerUser: Arbitrary[PlayerUser] = Arbitrary {
    user.arbitrary.map(user => PlayerUser(user.copy(role = Role.Player)))
  }

  implicit val userSignup: Arbitrary[SignupRequest] = Arbitrary[SignupRequest] {
    for {
      userName <- userNameGen
      password <- arbitrary[String]
      role <- arbitrary[Role]
    } yield SignupRequest(userName, password, role)
  }

  implicit val secureRandomId: Arbitrary[SecureRandomId] = Arbitrary[SecureRandomId] {
    arbitrary[String].map(SecureRandomId.apply)
  }

  implicit val jwtMac: Arbitrary[JWTMac[HMACSHA256]] = Arbitrary {
    for {
      key <- Gen.const(HMACSHA256.unsafeGenerateKey)
      claims <- Gen.finiteDuration.map(exp =>
        JWTClaims.withDuration[IO](expiration = Some(exp)).unsafeRunSync()
      )
    } yield JWTMacImpure
      .build[HMACSHA256](claims, key)
      .getOrElse(throw new Exception("Inconceivable"))
  }

  implicit def augmentedJWT[A, I](implicit
    arb1: Arbitrary[JWTMac[A]],
    arb2: Arbitrary[I]
  ): Arbitrary[AugmentedJWT[A, I]] =
    Arbitrary {
      for {
        id <- arbitrary[SecureRandomId]
        jwt <- arb1.arbitrary
        identity <- arb2.arbitrary
        expiry <- arbitrary[Instant]
        lastTouched <- Gen.option(arbitrary[Instant])
      } yield AugmentedJWT(id, jwt, identity, expiry, lastTouched)
    }
}

object Arbitraries extends SuperAmigosArbitraries