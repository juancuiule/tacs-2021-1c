package com.utn.tacs.domain.deck


import cats.Applicative

class DeckService[F[+_] : Applicative](repository: DeckRepository[F]) {

  def create(deck: Deck): F[Deck] = repository.create(deck)

  def getAll(pageSize: Int, offset: Int): F[List[Deck]] =
    repository.list(pageSize, offset)

  def get(id: Int): F[Option[Deck]] = repository.get(id)

  def getByName(name: String): F[Set[Deck]] = repository.findByName(name)

  def list(pageSize: Int, offset: Int): F[List[Deck]] = repository.list(pageSize, offset)
}

object DeckService {
  def apply[F[+_] : Applicative](repository: DeckRepository[F]) =
    new DeckService[F](repository)
}
