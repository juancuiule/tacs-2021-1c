package com.utn.tacs.domain.`match`

case class Match(
                  matchId: String,
                  player1Id: String,
                  player2Id: String,
                  deckId: String,
                  status: String, // ACTIVE,FINISHED,CANCELED
                  winnerId: String,
                ) {
}