package com.utn.tacs
package domain.auth

import domain.user.{Role, User}
import tsec.passwordhashers.PasswordHash

final case class LoginRequest(
  userName: String,
  password: String,
)

final case class SignupRequest(
  userName: String,
  password: String,
  role: Role
) {
  def asUser[A](hashedPassword: PasswordHash[A]): User = User(
    userName,
    hashedPassword.toString,
    role = role
  )
}