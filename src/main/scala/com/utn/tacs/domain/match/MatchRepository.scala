package com.utn.tacs.domain.`match`

import cats.data.{EitherT, OptionT}
import com.utn.tacs.domain.`match`.Match.MatchStep

trait MatchRepository[F[_]] {
  def getAll: F[List[Match]]

  def getMatch(matchId: String): EitherT[F, MatchNotFoundError.type, Match]

  def createMatch(newMatch: Match): F[Match]

  def updateMatch(upMatch: Match): OptionT[F, Match]

  def getMatchRounds(matchId: String): OptionT[F, List[MatchStep]]
}

