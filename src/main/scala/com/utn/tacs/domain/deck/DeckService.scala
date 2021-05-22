package com.utn.tacs.domain.deck


import cats.{Applicative, Monad}
import cats.data.OptionT

class DeckService[F[+_] : Applicative](repository: DeckRepository[F]) {

  def create(deck: Deck): F[Deck] = repository.create(deck)

  def getAll(pageSize: Int, offset: Int): F[List[Deck]] =
    repository.list(pageSize, offset)

  def get(id: Int): OptionT[F, Deck] = repository.get(id)

  def getByName(name: String): F[Set[Deck]] = repository.findByName(name)

  def list(pageSize: Int, offset: Int): F[List[Deck]] = repository.list(pageSize, offset)

  def addCard(id: Int, cardId: Int)(implicit m: Monad[F]): OptionT[F, Deck] = {
    for {
      deck <- repository.get(id)
      updatedDeck = deck.copy(cards = deck.cards + cardId)
      result <- repository.update(updatedDeck)
    } yield result
  }

  def removeCard(id: Int, cardId: Int)(implicit m: Monad[F]): OptionT[F, Deck] = {
    for {
      deck <- repository.get(id)
      updatedDeck = deck.copy(cards = deck.cards.filter(_ != cardId))
      result <- repository.update(updatedDeck)
    } yield result
  }

}

object DeckService {
  def apply[F[+_] : Applicative](repository: DeckRepository[F]) =
    new DeckService[F](repository)
}
