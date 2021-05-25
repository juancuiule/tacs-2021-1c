package com.utn.tacs.infrastructure.endpoint

import cats.data.Kleisli
import cats.effect.IO
import com.utn.tacs.domain.auth.{LoginRequest, SignupRequest}
import com.utn.tacs.domain.user.{Role, User}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.implicits._

trait LoginTest extends Http4sClientDsl[IO] with Http4sDsl[IO] {
  implicit val userEnc: EntityEncoder[IO, User] = jsonEncoderOf
  implicit val userDec: EntityDecoder[IO, User] = jsonOf

  implicit val signUpRequestEnc: EntityEncoder[IO, SignupRequest] = jsonEncoderOf
  implicit val signUpRequestDec: EntityDecoder[IO, SignupRequest] = jsonOf

  case class LoginResponseDTO(userName: String, id: Long, accessToken: String)

  implicit val loginResponseDec: EntityDecoder[IO, LoginResponseDTO] = jsonOf

  implicit val loginRequestEnc: EntityEncoder[IO, LoginRequest] = jsonEncoderOf
  implicit val loginRequestDec: EntityDecoder[IO, LoginRequest] = jsonOf

  def signUpAndLogIn(
    userSignUp: SignupRequest,
    userEndpoint: HttpApp[IO]
  ): IO[(LoginResponseDTO, Option[Authorization])] =
    for {
      signUpRq <- POST(userSignUp, uri"/users")
      signUpResp <- userEndpoint.run(signUpRq)
      user <- signUpResp.as[LoginResponseDTO]
      loginBody = LoginRequest(userSignUp.userName, userSignUp.password)
      loginRq <- POST(loginBody, uri"/users/login")
      loginResp <- userEndpoint.run(loginRq)
    } yield user -> loginResp.headers.get(Authorization)

  def signUpAndLogInAsAdmin(
    userSignUp: SignupRequest,
    userEndpoint: Kleisli[IO, Request[IO], Response[IO]]
  ): IO[(LoginResponseDTO, Option[Authorization])] =
    signUpAndLogIn(userSignUp.copy(role = Role.Admin), userEndpoint)

  def signUpAndLogInAsCustomer(
    userSignUp: SignupRequest,
    userEndpoint: Kleisli[IO, Request[IO], Response[IO]]
  ): IO[(LoginResponseDTO, Option[Authorization])] =
    signUpAndLogIn(userSignUp.copy(role = Role.Player), userEndpoint)
}