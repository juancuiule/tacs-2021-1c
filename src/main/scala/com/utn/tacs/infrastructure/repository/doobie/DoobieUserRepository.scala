package com.utn.tacs.infrastructure.repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import com.utn.tacs.domain.user.{Role, User, UserRepository}
import doobie._
import doobie.implicits._
import io.circe.parser.decode
import io.circe.syntax._
import tsec.authentication.IdentityStore

private object UserSQL {
  // H2 does not support JSON data type.
  implicit val roleMeta: Meta[Role] =
    Meta[String].imap(decode[Role](_).leftMap(throw _).merge)(_.asJson.toString)

  def insert(user: User): Update0 = sql"""
    INSERT INTO users (userName, passwordHash, role)
    VALUES (${user.userName}, ${user.passwordHash}, ${user.role})
  """.update

  def update(user: User, id: Long): Update0 = sql"""
    UPDATE users
    SET userName = ${user.userName}, passwordHash = ${user.passwordHash}, role = ${user.role}
    WHERE id = $id
  """.update

  def select(userId: Long): Query0[User] = sql"""
    SELECT userName, passwordHash, id, role
    FROM users
    WHERE id = $userId
  """.query

  def byUserName(userName: String): Query0[User] = sql"""
    SELECT userName, passwordHash, id, role
    FROM users
    WHERE userName = $userName
  """.query[User]

  def delete(userId: Long): Update0 = sql"""
    DELETE FROM users WHERE id = $userId
  """.update

  val selectAll: Query0[User] = sql"""
    SELECT userName, passwordHash, id, role
    FROM users
  """.query
}

class DoobieUserRepository[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends UserRepository[F]
    with IdentityStore[F, Long, User] { self =>
  import UserSQL._

  def create(user: User): F[User] =
    insert(user).withUniqueGeneratedKeys[Long]("id").map(id => user.copy(id = id.some)).transact(xa)

  def update(user: User): OptionT[F, User] =
    OptionT.fromOption[F](user.id).semiflatMap { id =>
      UserSQL.update(user, id).run.transact(xa).as(user)
    }

  def get(userId: Long): OptionT[F, User] = OptionT(select(userId).option.transact(xa))

  def findByUserName(userName: String): OptionT[F, User] =
    OptionT(byUserName(userName).option.transact(xa))

  def delete(userId: Long): OptionT[F, User] =
    get(userId).semiflatMap(user => UserSQL.delete(userId).run.transact(xa).as(user))

  def deleteByUserName(userName: String): OptionT[F, User] =
    findByUserName(userName).mapFilter(_.id).flatMap(delete)

//  def list(pageSize: Int, offset: Int): F[List[User]] =
//    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieUserRepository {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieUserRepository[F] =
    new DoobieUserRepository(xa)
}
