package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.cards.Card

case class Match(
                  var matchId: String,
                  player1Id: String,
                  player2Id: String,
                  deckId: String,
                  var status: String, // ACTIVE,FINISHED,CANCELED
                  var winnerId: String,
                  var roundIds: List[String] = List()
                ) {
  //TODO: private val _lastRound : Int

  def map[B](f: Match => B): B = f(this)

}
//TODO:falta agregar date a Match


case class Round(
                  id: Int,
                  matchId: String,
                  winnerId: String,
                  playerOneCard: Card,
                  playerTwoCard: Card
                ) //TODO:implementar