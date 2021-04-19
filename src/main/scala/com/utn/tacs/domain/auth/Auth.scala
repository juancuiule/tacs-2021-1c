package com.utn.tacs.domain.auth

import cats.Applicative
import cats.implicits._

trait Auth[F[_]] {
  def login(n: Auth.LoginData): F[Auth.LoginData]

  def signup(d: AnyVal): F[AnyVal]

  def logout(): F[AnyVal]
}

object Auth {
  implicit def apply[F[_]](implicit ev: Auth[F]): Auth[F] = ev

  def impl[F[_] : Applicative]: Auth[F] = new Auth[F] {
    def login(n: Auth.LoginData): F[Auth.LoginData] = {
      LoginData(n.username, n.password).pure[F]
    }

    def signup(d: AnyVal): F[AnyVal] = ???

    def logout(): F[AnyVal] = ???
  }

  final case class LoginData(username: String, password: String)
}