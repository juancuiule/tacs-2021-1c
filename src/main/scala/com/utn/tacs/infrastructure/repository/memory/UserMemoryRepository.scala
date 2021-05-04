package com.utn.tacs.infrastructure.repository.memory

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import com.utn.tacs.domain.user.{User, UserRepository}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class UserMemoryRepository[F[_] : Applicative]
  extends UserRepository[F]
    with IdentityStore[F, Long, User] {
  private val cache = new TrieMap[Long, User]

  private val random = new Random

  def create(user: User): F[User] = {
    val id = random.nextLong()
    val toSave = user.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def findByUserName(userName: String): OptionT[F, User] =
    OptionT.fromOption(cache.values.find(u => u.userName == userName))

  def update(user: User): OptionT[F, User] = OptionT {
    user.id.traverse { id =>
      cache.update(id, user)
      user.pure[F]
    }
  }

  def get(id: Long): OptionT[F, User] =
    OptionT.fromOption(cache.get(id))
}

object UserMemoryRepository {
  def apply[F[_] : Applicative]() =
    new UserMemoryRepository[F]
}