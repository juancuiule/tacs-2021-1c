package com.utn.tacs.domain.user

import cats.data.OptionT

trait UserRepository[F[_]] {
  def create(user: User): F[User]

  def update(user: User): OptionT[F, User]

  def get(userId: Long): OptionT[F, User]

  def findByUserName(userName: String): OptionT[F, User]
}