package com.utn.tacs.infrastructure.repository.memory

import com.utn.tacs.domain.cards.{Card, CardRepository}

import scala.collection.concurrent.TrieMap

class CardMemoryRepository extends CardRepository {

  private val cache = new TrieMap[Int, Card]()

  def create(card: Card): Card = {
    cache addOne (card.id -> card)
    card
  }

  def findByPublisher(publisher: String): List[Card] = cache.values.filter(_.biography.publisher.contains(publisher.toLowerCase)).toList

  def update(card: Card): Option[Card] = cache.replace(card.id, card)

  def get(id: Int): Option[Card] = cache.get(id)

  def getAll: List[Card] = cache.values.toList

  def delete(id: Int): Option[Card] = cache.remove(id)

  def findByName(name: String): Set[Card] =
    cache.values.filter(card => card.name == name).toSet

  def list(pageSize: Int, offset: Int): List[Card] = cache.values.slice(offset, offset + pageSize).toList
}

object CardMemoryRepository {
  def apply() = new CardMemoryRepository()
}