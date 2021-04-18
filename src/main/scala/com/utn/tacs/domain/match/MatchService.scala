package com.utn.tacs.domain.`match`

object MatchService {
  def getMatch(matchId : String) : Option[Match] = {
    val matchVal = new Match(matchId, "pepe", "roberto", "marvel_heroes", "active", null)
    return Option(matchVal)
  }
}
