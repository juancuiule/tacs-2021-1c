package com.utn.tacs.domain.`match`

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId

trait MatchService[F[_]] {
  def createMatch(player1Id: String, player2Id: String, deckId: String): F[Match]

  def getMatch(matchId: String): Option[Match]

  def withdraw(matchId: String, loserPlayer: String): F[Match]
}


object MatchService {

  def impl[F[_] : Applicative]: MatchService[F] = new MatchService[F] {
    def createMatch(player1Id: String, player2Id: String, deckId: String): F[Match] = {
      Match("match_id_test", player1Id, player2Id, deckId, "active", "no_winner_yet").pure[F]
    }

    override def getMatch(matchId: String): Option[Match] = {
      //TODO: validar que los playerID existan y  que deckId exista
      val matchVal = new Match(matchId, "pepe", "roberto", "marvel_heroes", "active", "")
      Some(matchVal)
    }

    override def withdraw(matchId: String, loserPlayer: String): F[Match] = {
      //TODO:implementar
      println(s"player: ${loserPlayer} withdraws the match: ${matchId}")
      Match(matchId, loserPlayer, "winner_player", "marvel", "finished", "winner_player").pure[F]
    }
  }


}
