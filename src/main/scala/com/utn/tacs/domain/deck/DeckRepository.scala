package com.utn.tacs.domain.deck

import cats.data.OptionT

trait DeckRepository[F[_]] {
  def create(deck: Deck): F[Deck]

  def update(deck: Deck): OptionT[F, Deck]

  def get(id: Int): OptionT[F, Deck]

  def getAll: F[List[Deck]]

  def delete(id: Int): OptionT[F, Deck]

  def findByName(name: String): F[Set[Deck]]

  def list(pageSize: Int, offset: Int): F[List[Deck]]
}

