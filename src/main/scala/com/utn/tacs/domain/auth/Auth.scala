package com.utn.tacs.domain.auth

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder, Json}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

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

  object LoginData {
    implicit val loginEncoder: Encoder[LoginData] = (a: LoginData) => Json.obj(
      ("username", Json.fromString(a.username)),
      ("password", Json.fromString(a.password)), // no devolver password
    )
    implicit val loginDecoder: Decoder[LoginData] = deriveDecoder[LoginData]

    implicit def loginEntityEncoder[F[_] : Applicative]: EntityEncoder[F, LoginData] =
      jsonEncoderOf[F, LoginData]

    implicit def loginEntityDecoder[F[_] : Sync]: EntityDecoder[F, LoginData] =
      jsonOf
  }
}