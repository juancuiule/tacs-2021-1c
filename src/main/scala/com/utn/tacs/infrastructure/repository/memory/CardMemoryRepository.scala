package com.utn.tacs.infrastructure.repository.memory

import cats.Applicative
import cats.data.OptionT
import com.utn.tacs.domain.cards.{Card, CardRepository}
import cats.implicits._

import scala.collection.concurrent.TrieMap

class CardMemoryRepository[F[_] : Applicative] extends CardRepository[F] {

  private val cache = new TrieMap[Int, Card]()

  def create(card: Card): F[Card] = {
    cache addOne (card.id -> card)
    card.pure[F]
  }

  def findByPublisher(publisher: String): F[List[Card]] = cache.values.filter(_.biography.publisher.contains(publisher.toLowerCase)).toList.pure[F]

  def update(card: Card): OptionT[F, Card] = {
    cache.replace(card.id, card)
    OptionT.fromOption(cache.get(card.id))
  }

  def get(id: Int): OptionT[F, Card] = OptionT.fromOption(cache.get(id))

  def getAll: F[List[Card]] = cache.values.toList.pure[F]

  def delete(id: Int): OptionT[F, Card] = OptionT.fromOption(cache.remove(id))

  def findByName(name: String): F[Set[Card]] =
    cache.values.filter(card => card.name == name).toSet.pure[F]

  def list(pageSize: Int, offset: Int): F[List[Card]] = cache.values.slice(offset, offset + pageSize).toList.pure[F]
}

object CardMemoryRepository {
  def apply[F[_] : Applicative]() = new CardMemoryRepository[F]()
}