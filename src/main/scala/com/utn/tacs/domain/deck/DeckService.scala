package com.utn.tacs.domain.deck


import cats.{Applicative, Monad}
import cats.data.{EitherT, OptionT}

trait DeckError

case object DeckAlreadyExists extends DeckError

class DeckService[F[+_] : Applicative](repository: DeckRepository[F])(implicit M: Monad[F]) {

  def create(deck: Deck): EitherT[F, DeckAlreadyExists.type, Deck] = {
    EitherT.liftF(repository.create(deck))
  }

  def getAll(pageSize: Int, offset: Int): F[List[Deck]] =
    repository.list(pageSize, offset)

  def get(id: Int): OptionT[F, Deck] = repository.get(id)

  def getByName(name: String): F[Set[Deck]] = repository.findByName(name)

  def list(pageSize: Int, offset: Int): F[List[Deck]] = repository.list(pageSize, offset)

  def addCard(id: Int, cardId: Int): OptionT[F, Deck] = {
    for {
      deck <- repository.get(id)
      result <- repository.addCard(deck, cardId)
    } yield result
  }

  def getDeckCards(id: Int): OptionT[F, List[Int]] = {
    for {
      deck <- repository.get(id)
    } yield deck.cards.toList
  }

  def removeCard(id: Int, cardId: Int): OptionT[F, Deck] = {
    for {
      deck <- repository.get(id)
      updatedDeck = deck.copy(cards = deck.cards.filter(_ != cardId))
      result <- repository.update(updatedDeck)
    } yield result
  }

}

object DeckService {
  def apply[F[+_] : Applicative](repository: DeckRepository[F])(implicit M: Monad[F]) =
    new DeckService[F](repository)
}
