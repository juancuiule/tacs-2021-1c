package com.utn.tacs.infrastructure.repository.memory

import cats.implicits._
import cats.Applicative
import com.utn.tacs.domain.deck.{Deck, DeckRepository}

import scala.collection.concurrent.TrieMap

class DeckMemoryRepository[F[_]: Applicative] extends DeckRepository[F] {
  private val cache = new TrieMap[Int, Deck]()

  def create(deck: Deck): F[Deck] = {
    (cache addOne (deck.id -> deck))
    deck.pure[F]
  }

  def update(deck: Deck): F[Option[Deck]] = cache.replace(deck.id, deck).pure[F]

  def get(id: Int): F[Option[Deck]] = cache.get(id).pure[F]

  def getAll: F[List[Deck]] = cache.values.toList.pure[F]

  def delete(id: Int): F[Option[Deck]] = cache.remove(id).pure[F]

  def findByName(name: String): F[Set[Deck]] =
    cache.values.filter(deck => deck.name == name).toSet.pure[F]

  def list(pageSize: Int, offset: Int): F[List[Deck]] = cache.values.slice(offset, offset + pageSize).toList.pure[F]
}

object DeckMemoryRepository {
  def apply[F[_]: Applicative]() = new DeckMemoryRepository[F]()
}