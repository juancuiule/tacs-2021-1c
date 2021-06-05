package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match.MatchStep


sealed trait MatchAction extends (Match => Match) {
  override def apply(v1: Match): (Match) = {
    v1.play(this)
  }
}

object MatchAction {
  case class Withdraw(loser: Long) extends MatchAction

  case class Battle(cardAttribute: String) extends MatchAction

  case object NoOp extends MatchAction

  case object DealCards extends MatchAction
}

sealed trait MatchState

object MatchState {
  case class BattleResult(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int])
    extends MatchState

  case class PreBattle(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int], player1Card: Int, player2Card: Int)
    extends MatchState

  case class Finished(winner: Long)
    extends MatchState

  case class Draw(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int]) extends MatchState
}


final case class Match(
  matchId: String,
  deck: Int,
  player1: Long,
  player2: Long,
  steps: List[MatchStep]
) {
  type MatchStep = (MatchAction, MatchState)

  import MatchAction._
  import MatchState._

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
      case (_: Draw, _) => prevState
      case (_, NoOp) => prevState
      case (_, Withdraw(loser)) =>
        println(s"withdraw player1: ${player1}, player2: ${player2}")
        println(s"withdraw loser: ${loser}")
        val winner = if (loser.equals(player1)) player2 else player1
        println(s"withdraw winner: ${winner}")
        Finished(winner)
      case (PreBattle(cardsInDeck, player1Cards, player2Cards, card1, card2), Battle(cardAttribute)) =>
        println(s"To battle by ${cardAttribute}")
        BattleResult(cardsInDeck, player1Cards :+ card1, player2Cards :+ card2)
//        if (card1.stats.get(cardAttribute) > card2.stats.get(cardAttribute)) {
//          BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards)
//        } else if (card1.stats.get(cardAttribute) < card2.stats.get(cardAttribute)) {
//          BattleResult(cardsInDeck, player1Cards, player2Cards :+ card1 :+ card2)
//        } else {
//          BattleResult(cardsInDeck, player1Cards :+ card1, player2Cards :+ card2)
//        }
      case (BattleResult(cards, cards1, cards2), DealCards) => {
        println(s"Dealing the remaining cards: ${cards.length}")
        cards.length match {
          case 0 | 1 =>
            if (cards1.length > cards2.length)
              Finished(player1)
            else if (cards2.length > cards1.length)
              Finished(player2)
            else
              Draw(cards, cards1, cards2)
          case _ =>
            val toDeal = cards.take(2)
            val toDeck = cards.drop(2)
            PreBattle(toDeck, cards1, cards2, toDeal.head, toDeal.last)
        }
      }
      case _ => prevState
    }
  }

  def currentState: MatchState = {
    steps.last._2
  }
}

object Match {
  type MatchStep = (MatchAction, MatchState)

  import MatchAction._
  import MatchState._

  def apply(
    matchId: String,
    deck: Int,
    player1: Long,
    player2: Long
  ): Match = {
    this (matchId, deck, player1, player2, List(
      (NoOp, BattleResult(List[Int](1, 2, 3), List[Int](), List[Int]()))
    )).play(DealCards)
  }
}

//object algo {
////  val stats: Stats = Stats(10, 10, 10, 10, 10, 10, 10)
////  val batmanCard: Card = Card(1, "Batman", stats, "url", Biography("Batman", "DC Comics"))
////  val supermanCard: Card = Card(2, "Superman", stats, "url", Biography("Superman", "DC Comics"))
////  val dcDeck: Deck = Deck(1, "DC Deck", Set(batmanCard, supermanCard).map(_.id))
////
////  val juan: User = User("Juan", "...", Some(1), Role.Player)
////  val pepe: User = User("Pepe", "...", Some(2), Role.Player)
//
//  val aMatch: Match = Match("1", ???, ???, ???)
//
//  val ejecutar = DealCards andThen Battle("height") andThen DealCards andThen Withdraw(???)
//  ejecutar(aMatch)
//}
