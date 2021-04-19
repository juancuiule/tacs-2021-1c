package com.utn.tacs.domain.`match`

case class Match(
                  matchId: String,
                  player1Id: String,
                  player2Id: String,
                  deckId: String,
                  status: String, // ACTIVE,FINISHED,CANCELED
                  winnerId: String,
                ) {
  def map[B](f: Match => B): B = f(this)
}


case class CreateMatch(
                        player1Id: String,
                        player2Id: String,
                        deckId: String,
                      )