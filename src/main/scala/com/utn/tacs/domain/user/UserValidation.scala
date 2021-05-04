package com.utn.tacs.domain.user

import cats.Applicative
import cats.data.EitherT
import cats.syntax.all._

sealed trait ValidationError extends Product with Serializable

case object UserNotFoundError extends ValidationError

case class UserAlreadyExistsError(user: User) extends ValidationError

case class UserAuthenticationFailedError(userName: String) extends ValidationError

class UserValidation[F[_] : Applicative](userRepo: UserRepository[F]) {
  def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit] =
    userRepo
      .findByUserName(user.userName)
      .map(UserAlreadyExistsError)
      .toLeft(())

  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, Unit] =
    userId match {
      case Some(id) =>
        userRepo
          .get(id)
          .toRight(UserNotFoundError)
          .void
      case None =>
        EitherT.left[Unit](UserNotFoundError.pure[F])
    }
}

object UserValidation {
  def apply[F[_] : Applicative](repo: UserRepository[F]): UserValidation[F] =
    new UserValidation[F](repo)
}