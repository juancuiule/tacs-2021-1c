package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match
import com.utn.tacs.domain.`match`.MatchState.{BattleResult, Draw, Finished, PreBattle}
import io.circe.Json

trait OutputMessage {
  val message: Option[Match]

  def forUser(targetUser: Long, inMatch: String): Boolean

  override def toString: String = message.fold(
    Json.obj(("matchId", Json.fromString("")))
  )(m => {
    val lastStep = m.currentState
    val state = lastStep match {
      case _: PreBattle => "preBattle"
      case _: Finished => "finished"
      case _: Draw => "draw"
      case _: BattleResult => "battleResult"
    }
    val cards = lastStep match {
      case ms: MatchState =>
        Json.obj(
          ("cardsInDeck", Json.fromInt(ms.cardsInDeck.length)),
          ("player1Cards", Json.fromInt(ms.player1Cards.length)),
          ("player2Cards", Json.fromInt(ms.player2Cards.length))
        )
    }
    val nextToPlay = lastStep match {
      case PreBattle(_, _, _, _, _, nextToPlay) => nextToPlay.toString
      case BattleResult(_, _, _, nextToPlay) => nextToPlay.toString
      case _ => ""
    }
    val winner = lastStep match {
      case Finished(_, _, _, winner) => winner.toString
      case _ => ""
    }
    val cardsInBattle = lastStep match {
      case PreBattle(_, _, _, player1Card, player2Card, _) => Some((player1Card, player2Card))
      case _ => None
    }
    Json.obj(
      ("matchId", Json.fromString(m.matchId)),
      ("deck", Json.fromInt(m.deck)),
      ("player1", Json.fromString(m.player1.toString)),
      ("player2", Json.fromString(m.player2.toString)),
      ("state", Json.obj(
        ("player1Card", Json.fromString(cardsInBattle.map(_._1.toString).getOrElse(""))),
        ("player2Card", Json.fromString(cardsInBattle.map(_._2.toString).getOrElse(""))),
        ("type", Json.fromString(state)),
        ("nextToPlay", Json.fromString(nextToPlay)),
        ("winner", Json.fromString(winner))
      ).deepMerge(cards))
    )
  }).toString()
}

case class SendToUser(user: Long, message: Option[Match]) extends OutputMessage {
  override def forUser(targetUser: Long, inMatch: String): Boolean = user == targetUser && message.exists(_.matchId == inMatch)
}

case class SendToUsers(users: Set[Long], message: Option[Match]) extends OutputMessage {
  override def forUser(targetUser: Long, inMatch: String): Boolean = users.contains(targetUser) && message.exists(_.matchId == inMatch)
}