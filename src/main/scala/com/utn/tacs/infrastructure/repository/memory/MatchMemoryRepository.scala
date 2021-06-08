package com.utn.tacs.infrastructure.repository.memory

import cats.Applicative
import cats.data.{EitherT, OptionT}
import cats.implicits._
import com.utn.tacs.domain.`match`._

import scala.collection.concurrent.TrieMap

class MatchMemoryRepository[F[_] : Applicative] extends MatchRepository[F] {

  private val matchDB = new TrieMap[String, Match]()

  def getMatch(matchId: String): EitherT[F, MatchNotFoundError.type, Match] =
    EitherT.fromOption(matchDB.get(matchId), MatchNotFoundError)

  def createMatch(newMatch: Match): F[Match] = {
    matchDB addOne (newMatch.matchId -> newMatch)
    newMatch.pure[F]
  }

  def getAll: F[List[Match]] = matchDB.values.toList.pure[F]

  def updateMatch(upMatch: Match): OptionT[F, Match] = {
    matchDB.replace(upMatch.matchId, upMatch)
    OptionT.fromOption(matchDB.get(upMatch.matchId))
  }

  def getMatchRounds(matchId: String): OptionT[F, List[(MatchAction, MatchState)]] = {
    val steps = matchDB.get(matchId).map(_.steps)
    OptionT.fromOption(steps)
  }
}

object MatchMemoryRepository {
  def apply[F[_] : Applicative]() = new MatchMemoryRepository[F]()
}