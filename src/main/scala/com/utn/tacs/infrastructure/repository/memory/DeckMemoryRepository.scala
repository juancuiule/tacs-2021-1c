package com.utn.tacs.infrastructure.repository.memory

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import com.utn.tacs.domain.deck.{Deck, DeckRepository}

import scala.collection.concurrent.TrieMap

class DeckMemoryRepository[F[_] : Applicative] extends DeckRepository[F] {
  private val cache = new TrieMap[Int, Deck]()

  cache.addOne((1, Deck(1, "spiderdeck", Set(625, 578, 500, 625, 578, 500, 625, 578, 500, 625, 578, 500))))

  def create(deck: Deck): F[Deck] = {
    (cache addOne (deck.id -> deck))
    deck.pure[F]
  }

  def update(deck: Deck): OptionT[F, Deck] = {
    cache.replace(deck.id, deck)
    OptionT.fromOption(cache.get(deck.id))
  }

  def get(id: Int): OptionT[F, Deck] = OptionT.fromOption(cache.get(id))

  def getAll: F[List[Deck]] = cache.values.toList.pure[F]

  def delete(id: Int): OptionT[F, Deck] = OptionT.fromOption(cache.remove(id))

  def findByName(name: String): F[Set[Deck]] =
    cache.values.filter(deck => deck.name == name).toSet.pure[F]

  def list(pageSize: Int, offset: Int): F[List[Deck]] = cache.values.slice(offset, offset + pageSize).toList.pure[F]
}

object DeckMemoryRepository {
  def apply[F[_] : Applicative]() = new DeckMemoryRepository[F]()
}