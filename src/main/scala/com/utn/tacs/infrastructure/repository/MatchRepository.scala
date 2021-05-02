package com.utn.tacs.infrastructure.repository

import com.utn.tacs.domain.`match`.{Match, MatchError, MatchRepository}

import scala.collection.concurrent.TrieMap

object MatchRepository extends MatchRepository {

  private val db = new TrieMap[String, Match]()

  def getMatch(matchId: String): Option[Match] = {
    db.get(matchId)
  }

  def createMatch(newMatch: Match): Match = {
    db.put(newMatch.matchId, newMatch)
    newMatch
  }

  def updateMatch(upMatch: Match): Either[MatchError, Match] = {

    if (!db.contains(upMatch.matchId)) {
      return Left(MatchNotFoundError)
    }
    db.put(upMatch.matchId, upMatch)
    Right(upMatch)
  }
}

case object MatchNotFoundError extends MatchError