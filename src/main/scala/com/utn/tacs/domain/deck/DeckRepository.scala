package com.utn.tacs.domain.deck

trait DeckRepository[F[_]] {
  def create(deck: Deck): F[Deck]

  def update(deck: Deck): F[Option[Deck]]

  def get(id: Int): F[Option[Deck]]

  def getAll: F[List[Deck]]

  def delete(id: Int): F[Option[Deck]]

  def findByName(name: String): F[Set[Deck]]

  def list(pageSize: Int, offset: Int): F[List[Deck]]
}

