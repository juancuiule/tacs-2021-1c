package com.utn.tacs.infrastructure.repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import com.utn.tacs.domain.cards.{Biography, Card, CardRepository, Stats}
import doobie._
import doobie.implicits._

private object CardSQL {
  implicit val han = LogHandler.jdkLogHandler

  val selectAll = {
    case class AuxCard(
      id: Int,
      name: String,
      image: String,
      fullname: String,
      publisher: String,
      height: Int,
      weight: Int,
      intelligence: Int,
      speed: Int,
      power: Int,
      combat: Int,
      strength: Int
    )
    sql"""
    select
           card.id,
           name,
           image,
           fullname,
           publisher,
           height,
           weight,
           intelligence,
           speed,
           power,
           combat,
           strength
    from superamigos.public.card
    join superamigos.public.biography b on b.id = card.id
    join superamigos.public.stat s on card.id = s.id;
  """.query[AuxCard].map(aux => Card(aux.id, aux.name,
      Stats(
        aux.height,
        aux.weight,
        aux.intelligence,
        aux.speed,
        aux.power,
        aux.combat,
        aux.strength
      ), aux.image, Biography(aux.fullname, aux.publisher))).to[List]
  }

  def insert(card: Card) = {
    print(card)
    val stats = card.stats
    val biography = card.biography
    sql"""insert into superamigos.public.card (id, name, image) values (${card.id}, ${card.name}, ${card.image})"""
      .update.run
      .flatMap { _ =>
        sql"""
        insert into superamigos.public.stat
        values (${card.id}, ${stats.height}, ${stats.weight}, ${stats.intelligence}, ${stats.speed}, ${stats.power}, ${stats.combat}, ${stats.strength})
      """.update.run.flatMap(_ =>
          sql"""
        insert into superamigos.public.biography
        values (${card.id}, ${biography.fullName}, ${biography.publisher})
      """.update.run)
      }
  }

  //  def select(cardId: Int): Query0[User] =
  //    sql"""
  //    SELECT userName, passwordHash, id, role
  //    FROM users
  //    WHERE id = $userId
  //  """.query
  //
  //  def delete(userId: Int): Update0 =
  //    sql"""
  //    DELETE FROM users WHERE id = $userId
  //  """.update
}

class DoobieCardRepository[F[_] : Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends CardRepository[F] {
  self =>

  import CardSQL._

  //
  //  def update(user: User): OptionT[F, User] =
  //    OptionT.fromOption[F](user.id).semiflatMap { id =>
  //      CardSQL.update(user, id).run.transact(xa).as(user)
  //    }
  //
  //  def get(userId: Long): OptionT[F, User] = OptionT(select(userId).option.transact(xa))
  //
  //  def findByUserName(userName: String): OptionT[F, User] =
  //    OptionT(byUserName(userName).option.transact(xa))
  //
  //  def delete(userId: Long): OptionT[F, User] =
  //    get(userId).semiflatMap(user => CardSQL.delete(userId).run.transact(xa).as(user))
  //
  //  def deleteByUserName(userName: String): OptionT[F, User] =
  //    findByUserName(userName).mapFilter(_.id).flatMap(delete)

  //  def list(pageSize: Int, offset: Int): F[List[User]] =
  //    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  override def create(card: Card): F[Card] =
    insert(card).map(id => card.copy(id = id)).transact(xa)

  override def findByPublisher(publisher: String): F[List[Card]] = selectAll.transact(xa)

  override def update(card: Card): OptionT[F, Card] = ???

  override def get(id: Int): OptionT[F, Card] = ???

  override def getAll: F[List[Card]] = selectAll.transact(xa)

  override def delete(id: Int): OptionT[F, Card] = ???

  override def findByName(name: String): F[Set[Card]] = ???

  override def list(pageSize: Int, offset: Int): F[List[Card]] = ???
}

object DoobieCardRepository {
  def apply[F[_] : Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieCardRepository[F] =
    new DoobieCardRepository(xa)
}
