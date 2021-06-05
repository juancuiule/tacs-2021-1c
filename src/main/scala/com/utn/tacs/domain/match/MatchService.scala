package com.utn.tacs.domain.`match`

import cats.data.EitherT
import cats.implicits.catsSyntaxApplicativeId
import cats.{Applicative, Monad}
import com.utn.tacs.domain.`match`.Match.MatchStep

import scala.util.Random


trait MatchError extends Serializable with Product

case object MatchNotFoundError extends MatchError

case object MatchAlreadyExistsError extends MatchError


class MatchService[F[+_] : Applicative](
  repository: MatchRepository,
  validation: MatchValidation[F]
) {
  def createMatch(player1: Long, player2: Long, deck: Int)(implicit M: Monad[F]): EitherT[F, MatchAlreadyExistsError.type, Match] = {
    val matchId = Random.alphanumeric.take(15).mkString("")
    val newMatch = Match(matchId, deck, player1, player2)
    for {
      _ <- validation.doesNotExist(newMatch)
      saved <- EitherT.liftF(repository.createMatch(newMatch).pure[F])
    } yield saved
  }

  def getMatch(matchId: String): EitherT[F, MatchNotFoundError.type, Match] = {
    EitherT.fromOptionF(repository.getMatch(matchId).pure[F], MatchNotFoundError)
  }

  def withdraw(matchId: String, loserPlayer: Long): EitherT[F, MatchNotFoundError.type, Match] = executeAction(matchId, MatchAction.Withdraw(loserPlayer))

  def executeAction(matchId: String, matchAction: MatchAction): EitherT[F, MatchNotFoundError.type, Match] = EitherT.fromEither {
    repository.getMatch(matchId).fold[Either[MatchNotFoundError.type, Match]](Left(MatchNotFoundError))(m => {
      val newMatch = m.play(matchAction)
      val updated = repository.updateMatch(newMatch)
      updated match {
        case None => Left(MatchNotFoundError)
        case Some(m) => Right(m)
      }
    })
  }

  def battleByAttribute(matchId: String, attribute: String): EitherT[F, MatchNotFoundError.type, Match] = executeAction(matchId, MatchAction.Battle(attribute))

  def getPlayedRounds(matchId: String): Option[List[MatchStep]] = {
    repository.getMatchRounds(matchId)
  }
}

object MatchService {
  def apply[F[+_] : Applicative](repository: MatchRepository, validation: MatchValidation[F]) =
    new MatchService[F](repository, validation)
}