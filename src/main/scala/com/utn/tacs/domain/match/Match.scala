package com.utn.tacs.domain.`match`
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

class Match(
             matchId: String,
             player1Id: String,
             player2Id: String,
             deckId: String,
             status: String, // ACTIVE,FINISHED,CANCELED
             winnerId: String,
           ) {

  def noseEsUnaPrueba(): String = {
    if (matchId != null) {
      if (player2Id != null && player1Id != null && deckId != null && status != null && winnerId != "") {
        return "hola mundo"
      }
    }
    return "noseee"
  }
}