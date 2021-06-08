package com.utn.tacs.domain.`match`

import cats.Applicative
import cats.data.EitherT
import cats.implicits._

class MatchValidation[F[_] : Applicative](repository: MatchRepository[F]) {
  def doesNotExist(_match: Match): EitherT[F, MatchAlreadyExistsError.type, Unit] = EitherT {
    repository
      .getMatch(_match.matchId).value.map {
      case Right(_) => Left(MatchAlreadyExistsError)
      case Left(_) => Right(())
    }
  }

  def exists(matchId: Option[String]): EitherT[F, MatchNotFoundError.type, Unit] = EitherT {
    matchId match {
      case Some(id) => repository.getMatch(id).value.map {
        case Right(_) => Right(())
        case _ => Left(MatchNotFoundError)
      }
      case _ => Either.left[MatchNotFoundError.type, Unit](MatchNotFoundError).pure[F]
    }
  }
}

object MatchValidation {
  def apply[F[_] : Applicative](repository: MatchRepository[F]) = new MatchValidation[F](repository)
}

