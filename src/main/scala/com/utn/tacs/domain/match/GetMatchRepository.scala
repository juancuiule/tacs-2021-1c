package com.utn.tacs.domain.`match`

trait GetMatchRepository {
  def getMatch(matchId: String): Option[Match]
}
