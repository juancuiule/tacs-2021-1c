package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match.MatchStep
import com.utn.tacs.domain.cards.Card
import com.utn.tacs.domain.deck.Deck
import com.utn.tacs.domain.user.User


sealed trait MatchAction extends (((Match, MatchState)) => (Match, MatchState)) {
  override def apply(v1: (Match, MatchState)): (Match, MatchState) = {
    val (partida, estado) = v1
    (partida, partida.nextStateFromAction(estado, this))
  }
}

sealed trait MatchState

case class Withdraw(loser: User) extends MatchAction

case class Battle(cardAttribute: String) extends MatchAction

final case class Match(
  matchId: String,
  deck: Deck,
  player1: User,
  player2: User,
  steps: List[MatchStep]
) {
  type MatchStep = (MatchAction, MatchState)

  def play(action: MatchAction): Match = {
    this.copy(steps = {
      val (_, prevStep) = steps.last
      val nextState = nextStateFromAction(prevStep, action)
      steps :+ ((action, nextState))
    })
  }

  def nextStateFromAction(prevState: MatchState, action: MatchAction): MatchState = {
    (prevState, action) match {
      case (_: Finished, _) => prevState
      case (_, NoOp) => prevState
      case (_, Withdraw(loser)) =>
        val winner = if (loser.id.equals(player1.id)) player2 else player1
        Finished(winner)
      case (PreBattle(cardsInDeck, player1Cards, player2Cards, card1, card2), Battle(_)) =>
        // Habría que tener en cuenta el cardaAttribute
        if (card1.stats.height > card2.stats.height) {
          BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards)
        } else if (card1.stats.height < card2.stats.height) {
          BattleResult(cardsInDeck, player1Cards, player2Cards :+ card1 :+ card2)
        } else {
          BattleResult(cardsInDeck, player1Cards :+ card1, player2Cards :+ card2)
        }
      case (BattleResult(cards, cards1, cards2), DealCards) => {
        cards.length match {
          case 0 | 1 =>
            val winner = if (cards1.length > cards2.length) player1 else player2
            Finished(winner)
          case _ =>
            val toDeal = cards.take(2)
            val toDeck = cards.drop(2)
            PreBattle(toDeck, cards1, cards2, toDeal.head, toDeal.last)
        }
      }
      case _ => prevState
    }
  }
}

object Match {
  type MatchStep = (MatchAction, MatchState)

  def apply(
    matchId: String,
    deck: Deck,
    player1: User,
    player2: User
  ): Match = {
    this (matchId, deck, player1, player2, List(
      (NoOp, BattleResult(List[Card](), List[Card](), List[Card]()))
    ))
  }
}

case class BattleResult(cardsInDeck: List[Card], player1Cards: List[Card], player2Cards: List[Card])
  extends MatchState

case class PreBattle(cardsInDeck: List[Card], player1Cards: List[Card], player2Cards: List[Card], player1Card: Card, player2Card: Card)
  extends MatchState

case class Finished(winner: User)
  extends MatchState

case object NoOp extends MatchAction

case object DealCards extends MatchAction


//object algo {
//  val stats: Stats = Stats(10, 10, 10, 10, 10, 10, 10)
//  val batmanCard: Card = Card(1, "Batman", stats, "url", Biography("Batman", "DC Comics"))
//  val supermanCard: Card = Card(2, "Superman", stats, "url", Biography("Superman", "DC Comics"))
//  val dcDeck: Deck = Deck(1, "DC Deck", Set(batmanCard, supermanCard).map(_.id))
//
//  val juan: User = User("Juan", "...", Some(1), Role.Player)
//  val pepe: User = User("Pepe", "...", Some(2), Role.Player)
//
//  val aMatch: Match = Match("1", dcDeck, juan, pepe)
//
//  val ejecutar: ((Match, MatchState)) => (Match, MatchState) = DealCards andThen Battle("height") andThen DealCards andThen Withdraw(juan)
//  ejecutar((aMatch, aMatch.steps.last._2))
//}
