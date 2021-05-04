package com.utn.tacs.infrastructure.repository.memory

import java.time.Instant

import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import tsec.authentication.{AugmentedJWT, BackingStore}
import tsec.common.SecureRandomId
import tsec.jws.JWSSerializer
import tsec.jws.mac.{JWSMacCV, JWSMacHeader, JWTMacImpure}
import tsec.mac.jca.{MacErrorM, MacSigningKey}

import scala.collection.concurrent.TrieMap

case class JWTInMemory(
  id: SecureRandomId,
  jwt: String,
  identity: Long,
  expiry: Instant,
  lastTouched: Option[Instant]
)

class AuthMemoryRepository[F[_] : Bracket[*[_], Throwable], A](
  val key: MacSigningKey[A]
)(implicit
  hs: JWSSerializer[JWSMacHeader[A]],
  s: JWSMacCV[MacErrorM, A]
) extends BackingStore[F, SecureRandomId, AugmentedJWT[A, Long]] {

  private val cache = new TrieMap[SecureRandomId, JWTInMemory]

  override def put(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]] = {
    val newJwt = JWTInMemory(jwt.id, jwt.jwt.toEncodedString, jwt.identity, jwt.expiry, jwt.lastTouched)
    cache.put(jwt.id, newJwt)
    jwt.pure[F]
  }


  override def update(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]] = this.put(jwt)

  override def get(id: SecureRandomId): OptionT[F, AugmentedJWT[A, Long]] = {
    OptionT.fromOption(cache.get(id)).semiflatMap {
      case JWTInMemory(_, jwtStringify, identity, expiry, lastTouched) =>
        JWTMacImpure.verifyAndParse(jwtStringify, key) match {
          case Left(err) => err.raiseError[F, AugmentedJWT[A, Long]]
          case Right(jwt) => AugmentedJWT(id, jwt, identity, expiry, lastTouched).pure[F]
        }
    }
  }

  override def delete(id: SecureRandomId): F[Unit] = {
    cache.remove(id)
    ().pure[F]
  }
}

object AuthMemoryRepository {
  def apply[F[_] : Bracket[*[_], Throwable], A](key: MacSigningKey[A])(implicit
    hs: JWSSerializer[JWSMacHeader[A]],
    s: JWSMacCV[MacErrorM, A]
  ): AuthMemoryRepository[F, A] =
    new AuthMemoryRepository(key)
}