package com.utn.tacs.domain.auth

import cats.Applicative
import cats.implicits._
import com.utn.tacs.domain.auth.Auth.LoginData

import scala.util.{Success, Try}

trait Auth[F[_]] {
  def login(n: Auth.LoginData): F[LoginData]

  def signup(d: Auth.LoginData): F[_]

  def logout(): Try[_]
}

object Auth {
  implicit def apply[F[_]](implicit ev: Auth[F]): Auth[F] = ev

  def impl[F[_] : Applicative]: Auth[F] = new Auth[F] {

    //private var repoUsers = ListBuffer(LoginData("John Doe", "12345678"))

    val johnDoe = LoginData("John Doe", "12345678")

    def login(n: LoginData): F[LoginData] = {

      val user = LoginData(n.username, n.password)
      if (user.equals(johnDoe)) user.pure[F] else LoginData("error","error").pure[F]
      //repoUsers.find(u => u.equals(user)).getOrElse(LoginError("User Not Found")).pure[F]
      // TODO deberia devolver un tipo LoginError si falla, capaz guardar una cookie en la sesion para los loggeados
      //      validar con algun repo como en cards o alguna api o un metodo de loggeo
    }

    def signup(d: LoginData): F[_] = {
      Success(d).pure[F]
    }

    /* repoUsers.find(u => u.equals(d)) match {
       case Some(foundUser) => SignupError("User already registered").pure[F]
       case None => {
         repoUsers += d
         d.pure[F]
         //FIXME se podria delegar en otro metodo. Deje el tipo con F[_] porque sino rompe todo y la verdad no se porque
       }
     }
   }*/

    def logout(): Try[_] = ???
  }

  case class LoginError(error: String)

  case class SignupError(error: String)

  final case class LoginData(username: String, password: String)

  final case class User(id: String, username: String, password: String)

}