package com.utn.tacs.infrastructure.endpoint

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.auth.{Auth, User}
import com.utn.tacs.domain.auth.Auth.UserDTO
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._


sealed trait ValidationError extends Product with Serializable

case class UserAlreadyExistsError(user: User) extends ValidationError

case class UserAuthenticationFailedError(userName: String) extends ValidationError

object AuthEndpoints {
  implicit def decoder[F[_] : Sync]: EntityDecoder[F, Auth.UserDTO] = jsonOf[F, Auth.UserDTO]

  def authRoutes[F[_] : Sync](A: Auth[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@POST -> Root / "login" =>
        val action = for {
          login <- EitherT.liftF(req.as[UserDTO])
          user <- A.login(login).leftMap(_ => UserAuthenticationFailedError(login.username))
        } yield (user)

        action.value.flatMap {
          case Right(user) => Ok(user.asJson)
          case Left(UserAuthenticationFailedError(name)) => BadRequest(s"Authenitcation failed for user $name")
        }
      case req@POST -> Root / "signup" =>
        val action = for {
          signup <- req.as[UserDTO]
          result <- A.signup(signup).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(_) => Conflict()
        }
      case POST -> Root / "logout" =>
        for {
          resp <- Ok("Logout Successful")
        } yield resp
    }
  }

}
