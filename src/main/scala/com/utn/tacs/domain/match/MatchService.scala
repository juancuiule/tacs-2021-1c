package com.utn.tacs.domain.`match`

import cats.effect.IO
import org.http4s.dsl.impl.UUIDVar

object MatchService[F[Match]] {
  def getMatch(matchId: String): Option[Match] = {
    val matchVal = new Match(matchId, "pepe", "roberto", "marvel_heroes", "active", "")
    return Some(matchVal)
  }

  def createMatch(player1Id: String, player2Id: String, deckId: String): F[Match] = {
    //TODO: validar que los playerID existan y  que deckId exista
    IO.pure(Match("", player1Id, player2Id, deckId, "active", ""))
  }

  def withdraw(matchId: String, playerId: String): Unit = {
    //TODO:implementar
    println(s"player: ${playerId} withdraws the match: ${matchId}")
  }
}
