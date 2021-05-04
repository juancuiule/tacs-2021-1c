package com.utn.tacs.domain.user

import cats.data._
import cats.{Functor, Monad}

class UserService[F[_]](userRepo: UserRepository[F], validation: UserValidation[F]) {
  def createUser(user: User)(implicit M: Monad[F]): EitherT[F, UserAlreadyExistsError, User] =
    for {
      _ <- validation.doesNotExist(user)
      saved <- EitherT.liftF(userRepo.create(user))
    } yield saved

  def getUser(userId: Long)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] =
    userRepo.get(userId).toRight(UserNotFoundError)

  def getUserByName(userName: String)(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] =
    userRepo.findByUserName(userName).toRight(UserNotFoundError)

  def update(user: User)(implicit M: Monad[F]): EitherT[F, UserNotFoundError.type, User] =
    for {
      _ <- validation.exists(user.id)
      saved <- userRepo.update(user).toRight(UserNotFoundError)
    } yield saved
}

object UserService {
  def apply[F[_]](repository: UserRepository[F], validation: UserValidation[F]): UserService[F] =
    new UserService[F](repository, validation)
}