package com.utn.tacs.domain.cards

import cats.data.OptionT

trait CardRepository[F[_]] {
  def create(card: Card): F[Card]

  def findByPublisher(publisher: String): F[List[Card]]

  def update(card: Card): OptionT[F, Card]

  def get(id: Int): OptionT[F, Card]

  def getAll: F[List[Card]]

  def delete(id: Int): OptionT[F, Card]

  def findByName(name: String): F[Set[Card]]

  def list(pageSize: Int, offset: Int): F[List[Card]]
}
