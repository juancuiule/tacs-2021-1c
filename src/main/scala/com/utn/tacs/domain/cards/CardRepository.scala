package com.utn.tacs.domain.cards

trait CardRepository {
  def create(card: Card): Card

  def findByPublisher(publisher: String): List[Card]

  def update(card: Card): Option[Card]

  def get(id: Int): Option[Card]

  def getAll: List[Card]

  def delete(id: Int): Option[Card]

  def findByName(name: String): Set[Card]

  def list(pageSize: Int, offset: Int): List[Card]
}
