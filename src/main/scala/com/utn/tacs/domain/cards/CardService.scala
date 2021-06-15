package com.utn.tacs.domain.cards

import cats.data.EitherT
//import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}

trait CardError extends Serializable with Product

case class CardAlreadyExistsError(card: Card) extends CardError

case object CardNotFoundError extends CardError

class CardService[F[+_] : Applicative](
  repository: CardRepository[F],
  validation: CardValidation[F]
) {
  def create(card: Card)(implicit M: Monad[F]): EitherT[F, CardAlreadyExistsError, Card] =
    for {
      _ <- validation.doesNotExist(card)
      saved <- EitherT.liftF(repository.create(card))
    } yield saved

  def getByPublisher(publisher: String): F[List[Card]] = repository.findByPublisher(publisher)

  def getAll(): F[List[Card]] = repository.getAll

  def get(id: Int): EitherT[F, CardNotFoundError.type, Card] = repository.get(id).toRight(CardNotFoundError)

  def getPublishers: F[List[String]] = {
    repository.getAll.map(cards => cards.map(_.biography.publisher).distinct)
  }

  def getByName(name: String): F[Set[Card]] = repository.findByName(name)

  def list(pageSize: Int, offset: Int): F[List[Card]] = repository.list(pageSize, offset)
}

object CardService {
  def apply[F[+_] : Applicative](repository: CardRepository[F], validation: CardValidation[F]) =
    new CardService[F](repository, validation)
}
