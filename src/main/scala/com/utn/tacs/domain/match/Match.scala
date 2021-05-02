package com.utn.tacs.domain.`match`

case class Match(
                  var matchId: String,
                  player1Id: String,
                  player2Id: String,
                  deckId: String,
                  var status: String, // ACTIVE,FINISHED,CANCELED
                  var winnerId: String,
                  var rounds: List[Round] = List()
                ) {
  //TODO: private val _lastRound : Int

  def map[B](f: Match => B): B = f(this)

  def addRound(round: Round): Unit = {
    //TODO: round.roundNumber = _lastRound + 1
    this.rounds = this.rounds ++ List(round)
  }
}
//TODO:falta agregar date a Match


case class Round() //TODO:implementar