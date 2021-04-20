package com.utn.tacs.domain.auth

import cats.{Applicative, Monad}
import cats.data.{EitherT, OptionT}
import cats.implicits._

import scala.collection.concurrent.TrieMap
import scala.util.Random

final case class User(id: Int, username: String, password: String)

class UserRepository[F[_] : Applicative] {
  // Mapa Int -> User
  private val cache = new TrieMap[Int, User]

  def getByUsername(username: String): OptionT[F, User] = {
    OptionT.fromOption(
      // dada la lista de users busca uno que tenga ese username
      cache.toList.find((item) => {
        val (_, user) = item
        user.username == username
      }).map(_._2) // el map (_._2) es un map(tupla => tupla._2) para quedarnos con el user
    )
  }

  def create(userDto: Auth.UserDTO): F[User] = {
    // id random
    val randomId = Random.nextInt()
    // arma el nuevo usuario
    val newUser = User(id = randomId, username = userDto.username, password = userDto.password)
    // lo guarda
    cache.put(randomId, newUser)
    // lo "mete" adentro de la caja F y lo devuelve
    newUser.pure[F]
  }
}

object UserRepository {
  def apply[F[_] : Applicative]() = new UserRepository[F]()
}

trait Auth[F[_]] {
  def login(userDto: Auth.UserDTO): EitherT[F, Auth.LoginError, User]

  def signup(userDto: Auth.UserDTO)(implicit M: Monad[F]): EitherT[F, Auth.SignupError, User]

  def logout(): F[_]
}

object Auth {
  implicit def apply[F[_]](implicit ev: Auth[F]): Auth[F] = ev

  def impl[F[_] : Applicative]: Auth[F] = new Auth[F] {
    val johnDoe: UserDTO = UserDTO("john_doe", "12345678")
    val userRepo: UserRepository[F] = UserRepository()
    userRepo.create(johnDoe)

    def login(userDto: UserDTO): EitherT[F, LoginError, User] = {
      userRepo
        .getByUsername(userDto.username) // un option
        .filter(_.password == userDto.password) // si matchea password (esto es así por ahora nada más)
        .toRight(LoginError("User not found or password error"))
      // trata de formar un Right(user) si no puede porque no hay user queda un Left(LoginError(...))

      // TODO: deberia devolver un tipo LoginError si falla, capaz guardar una cookie en la sesion para los loggeados
      // validar con algun repo como en cards o alguna api o un metodo de loggeo
    }

    def signup(userDto: UserDTO)(implicit M: Monad[F]): EitherT[F, SignupError, User] = for {
      _ <- userRepo
        .getByUsername(userDto.username)
        .map(_ => SignupError("Username already exists"))
        .toLeft(())
      saved <- EitherT.liftF(userRepo.create(userDto))
    } yield saved

    // Esto es lo mismo que lo que está arriba para signup
    // userRepo.getByUsername(userDto.username) // devuelve option de user
    //   .map(_ => SignupError("")) // si es un Some lo transforma en error poque queríamos encontrar un None
    //   .toLeft(()) // lo pasa a un Either[SignupError(...), User] que en este caso queda como Left(SignupError(...))
    //   .flatMap(_ =>
    //     EitherT.liftF(userRepo.create(userDto)) // crea el usar y lo envuelve en un Either
    //   )

    def logout(): F[_] = ???
  }

  case class LoginError(error: String)

  case class SignupError(error: String)

  final case class UserDTO(username: String, password: String)

}