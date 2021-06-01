package com.utn.tacs.domain.`match`

import cats.Applicative
import cats.data.EitherT
import cats.implicits._

class MatchValidation[F[_] : Applicative](repository: MatchRepository) {
  def doesNotExist(_match: Match): EitherT[F, MatchAlreadyExistsError.type , Unit] = EitherT.fromEither {
    repository.getMatch(_match.matchId).fold[Either[MatchAlreadyExistsError.type , Unit]](Right(()))(_ => Left(MatchAlreadyExistsError))
  }

  def exists(matchId: Option[String]): EitherT[F, MatchNotFoundError.type, Unit] = EitherT {
    matchId match {
      case Some(id) => repository.getMatch(id).pure[F].map {
        case Some(_) => Right(())
        case _ => Left(MatchNotFoundError)
      }
      case _ => Either.left[MatchNotFoundError.type, Unit](MatchNotFoundError).pure[F]
    }
  }
}

object MatchValidation {
  def apply[F[_] : Applicative](repository: MatchRepository) = new MatchValidation[F](repository)
}

