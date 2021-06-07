package com.utn.tacs.infrastructure.repository.memory

import com.utn.tacs.domain.`match`.Match.MatchStep
import com.utn.tacs.domain.`match`.{Match, MatchRepository}

import scala.collection.concurrent.TrieMap

class MatchMemoryRepository extends MatchRepository {

  private val matchDB = new TrieMap[String, Match]()

  def getMatch(matchId: String): Option[Match] = matchDB.get(matchId)

  def createMatch(newMatch: Match): Match = {
    matchDB addOne (newMatch.matchId -> newMatch)
    newMatch
  }

  def updateMatch(upMatch: Match): Option[Match] = {
    matchDB.replace(upMatch.matchId, upMatch)
    Some(upMatch)
  }

  def getMatchRounds(matchId: String): Option[List[MatchStep]] = {
    matchDB.get(matchId).map(_.steps)
  }
}

object MatchMemoryRepository {
  def apply() = new MatchMemoryRepository()
}