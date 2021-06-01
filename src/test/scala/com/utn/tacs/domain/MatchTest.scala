package com.utn.tacs.domain

import com.utn.tacs.SuperAmigosArbitraries
import com.utn.tacs.domain.`match`.MatchAction.{Battle, DealCards, NoOp}
import com.utn.tacs.domain.`match`.MatchState.BattleResult
import com.utn.tacs.domain.cards.{Biography, Stats}
//import com.utn.tacs.domain.`match`.MatchState
import com.utn.tacs.domain.`match`.Match
import com.utn.tacs.domain.cards.Card
import com.utn.tacs.domain.deck.Deck
import com.utn.tacs.domain.user._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class MatchTest
  extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with SuperAmigosArbitraries {

  test("create user and log in") {

    val stats10: Stats = Stats(10, 10, 10, 10, 10, 10, 10)
    val batmanCard: Card = Card(1, "Batman", stats10, "url", Biography("Batman", "DC Comics"))
    val aquamanCard: Card = Card(2, "Aquaman", stats10, "url", Biography("Aquaman", "DC Comics"))

    val stats20: Stats = Stats(20, 10, 10, 10, 10, 10, 10)
    val supermanCard: Card = Card(3, "Superman", stats20, "url", Biography("Superman", "DC Comics"))
    val dcDeck: Deck = Deck(1, "DC Deck", Set(batmanCard, supermanCard, aquamanCard).map(_.id))

    val juan: User = User("Juan", "...", Some(1), Role.Player)
    val pepe: User = User("Pepe", "...", Some(2), Role.Player)

    val aMatch: Match = Match("1", dcDeck, juan, pepe, List(
      (NoOp, BattleResult(List(batmanCard, supermanCard, aquamanCard), List(), List()))
    ))

    val ejecutar = DealCards andThen Battle("height") andThen DealCards
    val result = ejecutar(aMatch)

    println(result.currentState)
  }
}