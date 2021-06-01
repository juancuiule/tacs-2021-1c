package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.cards.Card

//TODO: private val _lastRound : Int
final case class Match(
  matchId: String,
  player1Id: String,
  player2Id: String,
  deckId: String,
  status: String, // ACTIVE,FINISHED,CANCELED
  winnerId: String,
  roundIds: List[String] = List()
)

//TODO:falta agregar date a Match
final case class Round(
  id: Int,
  matchId: String,
  winnerId: String,
  playerOneCard: Card,
  playerTwoCard: Card
)
//TODO:implementar