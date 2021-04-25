package com.utn.tacs.domain.cards

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}


trait CardError extends Serializable with Product

case class CardAlreadyExistsError(card: Card) extends CardError

case object CardNotFoundError extends CardError

class CardService[F[+_]](
                          repository: CardRepository[F],
                          validation: CardValidation[F]
                        ) {

  def create(card: Card)(implicit M: Monad[F]): EitherT[F, CardAlreadyExistsError, Card] =
    for {
      _ <- validation.doesNotExist(card)
      saved <- EitherT.liftF(repository.create(card))
    } yield saved

  def getByPublisher(publisher: String): F[List[Card]] = repository.findByPublisher(publisher)

  def getAll(pageSize: Int, offset: Int): F[List[Card]] =
    repository.list(pageSize, offset)

  def get(id: Int)(implicit FF: Sync[F]): EitherT[F, CardNotFoundError.type, Card] = EitherT.fromOptionF(repository.get(id), CardNotFoundError)

  def getPublishers(implicit FF: Applicative[F]): F[List[String]] = {
    repository.getAll.map(cardList => cardList.mapFilter(card => card.biography.map(_.publisher))).map(_.toSet.toList)
  }

  def getByName(name: String): F[Set[Card]] = repository.findByName(name)

  def list(pageSize: Int, offset: Int): F[List[Card]] = repository.list(pageSize, offset)
}

object CardService {
  def apply[F[+_]](repository: CardRepository[F], validation: CardValidation[F]) =
    new CardService[F](repository, validation)
}