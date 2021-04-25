package com.utn.tacs.domain.cards

trait CardRepository[F[_]] {
  def create(card: Card): F[Card]

  def findByPublisher(publisher: String): F[List[Card]]

  def update(card: Card): F[Option[Card]]

  def get(id: Int): F[Option[Card]]

  def getAll: F[List[Card]]

  def delete(id: Int): F[Option[Card]]

  def findByName(name: String): F[Set[Card]]

  def list(pageSize: Int, offset: Int): F[List[Card]]
}
