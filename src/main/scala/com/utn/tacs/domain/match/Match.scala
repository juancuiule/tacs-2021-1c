package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match.MatchStep


sealed trait MatchAction

object MatchAction {
  case class Withdraw(loser: Long) extends MatchAction

  case class Battle(cardAttribute: String) extends MatchAction

  case object NoOp extends MatchAction

  case object InitMatch extends MatchAction

  case object DealCards extends MatchAction
}

sealed trait MatchState {
  val cardsInDeck: List[Int]
  val player1Cards: List[Int]
  val player2Cards: List[Int]
}


object MatchState {
  def unapply(state: MatchState): Option[(List[Int], List[Int], List[Int])] = Some((state.cardsInDeck, state.player1Cards, state.player2Cards))

  case class BattleResult(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int], nextToPlay: Long)
    extends MatchState

  case class PreBattle(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int], player1Card: Int, player2Card: Int, nextToPlay: Long)
    extends MatchState

  case class Finished(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int], winner: Long)
    extends MatchState

  case class Draw(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int])
    extends MatchState
}


final case class Match(
  matchId: String,
  deck: Int,
  player1: Long,
  player2: Long,
  steps: List[MatchStep]
) {
  type MatchStep = (MatchAction, MatchState)

  def players = Set(player1, player2)

  def hasPlayer(player: Long): Boolean = {
    player1 == player || player2 == player
  }

  def currentState: MatchState = {
    steps.last._2
  }
}

object Match {
  type MatchStep = (MatchAction, MatchState)

  def apply(
    matchId: String,
    deck: Int,
    player1: Long,
    player2: Long
  ): Match = {
    this (matchId, deck, player1, player2, List())
  }
}
