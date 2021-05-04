package com.utn.tacs.infrastructure.repository

import com.utn.tacs.domain.`match`.{Match, MatchError, MatchRepository, Round}

import scala.collection.concurrent.TrieMap

object MatchRepository extends MatchRepository {

  private val matchDB = new TrieMap[String, Match]()
  private val roundsDB = new TrieMap[String, List[Round]]()

  def getMatch(matchId: String): Option[Match] = {
    matchDB.get(matchId)
  }

  def createMatch(newMatch: Match): Match = {
    matchDB.put(newMatch.matchId, newMatch)
    newMatch
  }

  def updateMatch(upMatch: Match): Either[MatchError, Match] = {

    if (!matchDB.contains(upMatch.matchId)) {
      return Left(MatchNotFoundError)
    }
    matchDB.put(upMatch.matchId, upMatch)
    Right(upMatch)
  }

  def getMatchRounds(matchId: String): List[Round] = {
    roundsDB.getOrElse(matchId, List())
  }
}

case object MatchNotFoundError extends MatchError