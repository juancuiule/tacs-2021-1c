package com.utn.tacs.domain.user

import cats.Applicative
import tsec.authorization.AuthorizationInfo

case class User(
                 userName: String,
                 passwordHash: String,
                 id: Option[Long] = None,
                 role: Role
               )

object User {
  implicit def authRole[F[_]](implicit F: Applicative[F]): AuthorizationInfo[F, Role, User] =
    new AuthorizationInfo[F, Role, User] {
      def fetchInfo(user: User): F[Role] = F.pure(user.role)
    }

}