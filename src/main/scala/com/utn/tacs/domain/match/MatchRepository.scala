package com.utn.tacs.domain.`match`

trait MatchRepository {
  def getMatch(matchId: String): Option[Match]

  def createMatch(newMatch: Match): Match

  def updateMatch(upMatch: Match): Either[MatchError, Match]

  def getMatchRounds(matchId: String): List[Round]
}

trait MatchError extends Serializable with Product



