package com.utn.tacs.domain.`match`

trait MatchRepository {
  def getMatch(matchId: String): Option[Match]

  def createMatch(newMatch: Match): Match

  def updateMatch(upMatch: Match): Either[MatchError, Match]
}

trait MatchError extends Serializable with Product



