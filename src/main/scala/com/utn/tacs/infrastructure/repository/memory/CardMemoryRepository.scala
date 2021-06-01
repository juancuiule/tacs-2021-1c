package com.utn.tacs.infrastructure.repository.memory

import com.utn.tacs.domain.cards.{Biography, Card, CardRepository, Stats}

import scala.collection.concurrent.TrieMap

class CardMemoryRepository extends CardRepository {

  private val cache = new TrieMap[Int, Card]()

  cache.addOne((
    625,
    Card(
      625,
      "Spider-Woman III",
      Stats(173, 55, 50, 27, 60, 28, 48),
      "https://www.superherodb.com/pictures2/portraits/10/100/482.jpg",
      Biography("Martha Franklin", "Marvel Comics")
    )
  ))

  cache.addOne((
    578,
    Card(
      578,
      "Scarlet Spider II",
      Stats(193, 113, 88, 60, 37, 56, 55),
      "https://www.superherodb.com/pictures2/portraits/10/100/1536.jpg",
      Biography("Kaine Parker", "Marvel Comics")
    )
  ))

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