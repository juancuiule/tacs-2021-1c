package com.utn.tacs.domain.`match`

trait MatchRepository {
  def getMatch(matchId: String): Option[Match]

  def createMatch(newMatch: Match): Match

  def updateMatch(upMatch: Match): Option[Match]

  def getMatchRounds(matchId: String): Option[List[Round]]
}

