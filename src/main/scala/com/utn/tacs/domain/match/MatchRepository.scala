package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match.MatchStep

trait MatchRepository {
  def getAll: List[Match]

  def getMatch(matchId: String): Option[Match]

  def createMatch(newMatch: Match): Match

  def updateMatch(upMatch: Match): Option[Match]

  def getMatchRounds(matchId: String): Option[List[MatchStep]]
}

