package com.utn.tacs.infrastructure.repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import cats.free.Free
import com.utn.tacs.domain.cards.{Biography, Card, CardRepository, Stats}
import doobie._
import doobie.free.connection
import doobie.implicits._

private object CardSQL {
  implicit val han: LogHandler = LogHandler.jdkLogHandler
  val selectAll: doobie.ConnectionIO[List[Card]] = {
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
  """.query[JoinedCard].map(_.toCard).to[List]
  }

  def insert(card: Card): Free[connection.ConnectionOp, Int] = {
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

  def select(cardId: Int): Query0[Card] =
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
    join superamigos.public.stat s on card.id = s.id
    where card.id = ${cardId};
  """.query[JoinedCard].map(_.toCard)

  def selectByPublisher(publisher: String): doobie.ConnectionIO[List[Card]] =
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
    join superamigos.public.stat s on card.id = s.id
    where publisher = ${publisher};
  """.query[JoinedCard].map(_.toCard).to[List]

  case class JoinedCard(
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
  ) {
    def toCard: Card = Card(
      this.id,
      this.name,
      Stats(this.height, this.weight, this.intelligence, this.speed, this.power, this.combat, this.strength),
      this.image,
      Biography(this.fullname, this.publisher)
    )
  }
}

class DoobieCardRepository[F[_] : Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends CardRepository[F] {
  self =>

  import CardSQL._

  override def create(card: Card): F[Card] =
    insert(card).map(id => card.copy(id = id)).transact(xa)

  override def findByPublisher(publisher: String): F[List[Card]] = selectByPublisher(publisher).transact(xa)

  override def get(id: Int): OptionT[F, Card] = OptionT(select(id).option.transact(xa))

  override def getAll: F[List[Card]] = selectAll.transact(xa)

  override def update(card: Card): OptionT[F, Card] = ???

  override def delete(id: Int): OptionT[F, Card] = ???

  override def findByName(name: String): F[Set[Card]] = ???

  override def list(pageSize: Int, offset: Int): F[List[Card]] = ???
}

object DoobieCardRepository {
  def apply[F[_] : Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieCardRepository[F] =
    new DoobieCardRepository(xa)
}
