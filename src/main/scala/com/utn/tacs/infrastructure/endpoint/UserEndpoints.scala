package com.utn.tacs.infrastructure.endpoint

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import com.utn.tacs.domain.auth.{LoginRequest, SignupRequest}
import com.utn.tacs.domain.user.{User, UserAlreadyExistsError, UserAuthenticationFailedError, UserService}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication._
import tsec.common.Verified
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

class UserEndpoints[F[_] : Sync, A, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  implicit val userDecoder: EntityDecoder[F, User] = jsonOf
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf

  implicit val signupReqDecoder: EntityDecoder[F, SignupRequest] = jsonOf

  private def loginEndpoint(
    userService: UserService[F],
    cryptService: PasswordHasher[F, A],
    auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    HttpRoutes.of[F] { case req@POST -> Root / "login" =>
      val action = for {
        login <- EitherT.liftF(req.as[LoginRequest])
        name = login.userName
        user <- userService.getUserByName(name).leftMap(_ => UserAuthenticationFailedError(name))
        checkResult <- EitherT.liftF(
          cryptService.checkpw(login.password, PasswordHash[A](user.passwordHash))
        )
        _ <-
          if (checkResult == Verified) EitherT.rightT[F, UserAuthenticationFailedError](())
          else EitherT.leftT[F, User](UserAuthenticationFailedError(name))
        token <- user.id match {
          case None => throw new Exception("Impossible") // User is not properly modeled
          case Some(id) => EitherT.right[UserAuthenticationFailedError](auth.create(id))
        }
      } yield (user, token)

      action.value.flatMap {
        case Right((user, token)) => {
          Ok(LoginResponseDTO(user.userName, user.id.get.toString, token.jwt.toEncodedString).asJson).map(auth.embed(_, token))
        }
        case Left(UserAuthenticationFailedError(name)) =>
          BadRequest(Json.obj(
            ("error", Json.fromString(s"Authentication failed for user $name"))
          ))

      }
    }

  case class LoginResponseDTO(userName: String, id: String, accessToken: String)


  private def signupEndpoint(
    userService: UserService[F],
    crypt: PasswordHasher[F, A],
    auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    HttpRoutes.of[F] { case req@POST -> Root =>
      val action = for {
        signup <- req.as[SignupRequest]
        hash <- crypt.hashpw(signup.password)
        user <- signup.asUser(hash).pure[F]
        result <- userService.createUser(user).value
      } yield result

      action.flatMap {
        case Right(saved) => {
          val firstToken = for {
            firstToken <- saved.id match {
              case None => throw new Exception("Impossible") // User is not properly modeled
              case Some(id) => EitherT.right[UserAuthenticationFailedError](auth.create(id))
            }
          } yield firstToken

          firstToken.value.flatMap {
            case Right(token) => Ok(LoginResponseDTO(saved.userName, saved.id.get.toString, token.jwt.toEncodedString).asJson).map(auth.embed(_, token))
            case Left(_) => ???
          }
        }
        case Left(UserAlreadyExistsError(existing)) =>
          Conflict(Json.obj(("error", Json.fromString(s"The user with user name ${existing.userName} already exists"))))
      }
    }


  def endpoints(
    userService: UserService[F],
    cryptService: PasswordHasher[F, A],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    loginEndpoint(userService, cryptService, auth.authenticator) <+> signupEndpoint(userService, cryptService, auth.authenticator)
  }
}

object UserEndpoints {
  def endpoints[F[_] : Sync, A, Auth: JWTMacAlgo](
    userService: UserService[F],
    cryptService: PasswordHasher[F, A],
    auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new UserEndpoints[F, A, Auth].endpoints(userService, cryptService, auth)
}