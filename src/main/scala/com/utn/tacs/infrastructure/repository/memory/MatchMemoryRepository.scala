package com.utn.tacs.infrastructure.repository.memory

import com.utn.tacs.domain.`match`.Match.MatchStep
import com.utn.tacs.domain.`match`.{Match, MatchRepository}

import scala.collection.concurrent.TrieMap

class MatchMemoryRepository extends MatchRepository {

  private val matchDB = new TrieMap[String, Match]()
  private val roundsDB = new TrieMap[String, List[MatchStep]]()

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
    roundsDB.get(matchId)
  }
}

object MatchMemoryRepository {
  def apply() = new MatchMemoryRepository()
}