package com.utn.tacs.domain.`match`

import cats.{Applicative, Monad}
import cats.data.EitherT
import cats.implicits.catsSyntaxApplicativeId

import scala.util.Random


trait MatchError extends Serializable with Product
case object MatchNotFoundError extends MatchError
case class MatchAlreadyExistsError(`match`: Match) extends MatchError


class MatchService[F[+_] : Applicative](
  repository: MatchRepository,
  validation: MatchValidation[F]
) {
  def createMatch(player1Id: String, player2Id: String, deckId: String)(implicit M: Monad[F]): EitherT[F, MatchAlreadyExistsError, Match] = {
    val matchId = Random.alphanumeric.take(15).mkString("")
    val newMatch = Match(matchId, player1Id, player2Id, deckId, "active", "no_winner_yet")
    for {
      _ <- validation.doesNotExist(newMatch)
      saved <- EitherT.liftF(repository.createMatch(newMatch).pure[F])
    } yield saved
  }

  def getMatch(matchId: String): EitherT[F, MatchNotFoundError.type, Match] = {
    EitherT.fromOptionF(repository.getMatch(matchId).pure[F], MatchNotFoundError)
  }
//
//  def withdraw(matchId: String, loserPlayer: String): Either[MatchError, Match] = {
//    repository.getMatch(matchId)
//      .map(m => {
//        val winnerId = if (m.player1Id.equals(loserPlayer)) {
//          m.player2Id
//        } else {
//          m.player1Id
//        }
//        m.copy(status = "finished", winnerId = winnerId)
//
//        val updateResult: Option[Match] = repository.updateMatch(m)
//        updateResult match {
//          case None => Left(MatchNotFoundError)
//          case Some(m) => Right(m)
//        }
//      }).getOrElse(Left(MatchNotFoundError))
//  }
//
//  def getPlayedRounds(matchId: String): List[Round] = {
//    repository.getMatchRounds(matchId)
//  }
}

object MatchService {
  def apply[F[+_] : Applicative](repository: MatchRepository, validation: MatchValidation[F]) =
    new MatchService[F](repository, validation)
}