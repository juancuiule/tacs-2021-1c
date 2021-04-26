package com.utn.tacs.domain.cards

import cats.Applicative
import cats.data.EitherT
import cats.implicits._

class CardValidation[F[_] : Applicative](repository: CardRepository) {
  def doesNotExist(card: Card): EitherT[F, CardAlreadyExistsError, Unit] = EitherT {
    repository.findByName(card.name).pure[F].map {
      cards =>
        if (cards.forall(c => c.biography != card.biography))
          Right(())
        else
          Left(CardAlreadyExistsError(card))
    }
  }

  def exists(cardId: Option[Int]): EitherT[F, CardNotFoundError.type, Unit] = EitherT {
    cardId match {
      case Some(id) => repository.get(id).pure[F].map {
        case Some(_) => Right(())
        case _ => Left(CardNotFoundError)
      }
      case _ => Either.left[CardNotFoundError.type, Unit](CardNotFoundError).pure[F]
    }
  }
}

object CardValidation {
  def apply[F[_] : Applicative](repository: CardRepository) = new CardValidation[F](repository)
}