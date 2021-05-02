package com.utn.tacs.domain.cards

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}


trait CardError extends Serializable with Product

case class CardAlreadyExistsError(card: Card) extends CardError

case object CardNotFoundError extends CardError

class CardService[F[+_] : Applicative](
                                        repository: CardRepository,
                                        validation: CardValidation[F]
                                      ) {

  def create(card: Card)(implicit M: Monad[F]): EitherT[F, CardAlreadyExistsError, Card] =
    for {
      _ <- validation.doesNotExist(card)
      saved <- EitherT.liftF(repository.create(card).pure[F])
    } yield saved

  def getByPublisher(publisher: String): F[List[Card]] = repository.findByPublisher(publisher).pure[F]

  def getAll(pageSize: Int, offset: Int): F[List[Card]] =
    repository.list(pageSize, offset).pure[F]

  def get(id: Int)(implicit FF: Sync[F]): EitherT[F, CardNotFoundError.type, Card] = EitherT.fromOptionF(repository.get(id).pure[F], CardNotFoundError)

  def getPublishers: F[List[String]] = {
    repository.getAll.map(card => card.biography.publisher)
      .distinct
      .pure[F]
  }

  def getByName(name: String): F[Set[Card]] = repository.findByName(name).pure[F]

  def list(pageSize: Int, offset: Int): F[List[Card]] = repository.list(pageSize, offset).pure[F]
}

object CardService {
  def apply[F[+_] : Applicative](repository: CardRepository, validation: CardValidation[F]) =
    new CardService[F](repository, validation)
}
