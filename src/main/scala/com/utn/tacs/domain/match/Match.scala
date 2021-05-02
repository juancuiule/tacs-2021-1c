package com.utn.tacs.domain.`match`

case class Match(
             var matchId: String,
             player1Id: String,
             player2Id: String,
             deckId: String,
             var status: String, // ACTIVE,FINISHED,CANCELED
             winnerId: String,
           ) {
  def map[B](f: Match => B): B = f(this)
}

/*class Match {
  var id: String

  def getId(): String = {
    id
  }
}*/

case class CreateMatch(
                        player1Id: String,
                        player2Id: String,
                        deckId: String,
                      )