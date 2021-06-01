package com.utn.tacs.infrastructure.repository.memory

import com.utn.tacs.domain.`match`.{Match, MatchRepository}

import scala.collection.concurrent.TrieMap

class MatchMemoryRepository extends MatchRepository {

  private val matchDB = new TrieMap[String, Match]()
//  private val roundsDB = new TrieMap[String, List[Round]]()

  def getMatch(matchId: String): Option[Match] = matchDB.get(matchId)

  def createMatch(newMatch: Match): Match = {
    matchDB addOne (newMatch.matchId -> newMatch)
    newMatch
  }

  def updateMatch(upMatch: Match): Option[Match] = {
    matchDB.replace(upMatch.matchId, upMatch)
  }

//  def getMatchRounds(matchId: String): List[Round] = {
//    roundsDB.getOrElse(matchId, List())
//  }
}

object MatchMemoryRepository {
  def apply() = new MatchMemoryRepository()
}