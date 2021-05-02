package com.utn.tacs.domain.`match`

import scala.collection.concurrent.TrieMap
import scala.util.Random

trait MatchRepository {
  def getMatch(matchId: String): Option[Match]

  def createMatch(newMatch: Match): Match

  def updateMatch(upMatch: Match): Either[MatchError, Match]
}

object MatchRepository extends MatchRepository {

  private val db = new TrieMap[String, Match]()

  def getMatch(matchId: String): Option[Match] = {
    db.get(matchId)
  }

  def createMatch(newMatch: Match): Match = {
    val id = Random.alphanumeric.take(15).mkString("")
    newMatch.matchId = id
    db.put(id, newMatch)
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


trait MatchError extends Serializable with Product

case class CardAlreadyExistsError(m: Match) extends MatchError

case object MatchNotFoundError extends MatchError


