package com.utn.tacs.infrastructure.repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import cats.free.Free
import cats.implicits._
import com.utn.tacs.domain.deck.{Deck, DeckRepository}
import doobie._
import doobie.free.connection
import doobie.implicits._

private object DeckSQL {
  implicit val han: LogHandler = LogHandler.jdkLogHandler

  val selectAll: doobie.ConnectionIO[List[Deck]] = {
    sql"""
    select id, name, card_id as card
    from superamigos.public.deck
    left join superamigos.public.card_x_deck cxd on deck.id = cxd.deck_id
  """.query[(Int, String, Option[Int])].to[List].map(results => results.groupBy(x => (x._1, x._2)).map(g => {
      val (id, name) = g._1
      val rows = g._2
      val cards = rows.map(row => row._3).filter(_.isDefined).map(_.get)
      Deck(id, name, cards.toSet)
    }).toList)
  }

  def insert(deck: Deck): doobie.ConnectionIO[Deck] = {
    val createDeck =
      sql"""
         insert into superamigos.public.deck (id, name)
         values (${deck.id}, ${deck.name})
       """.update.run
    val sql = "insert into superamigos.public.card_x_deck (deck_id, card_id) values (?, ?)"
    val createCardsXDeck = Update[(Int, Int)](sql).updateMany(deck.cards.toList.map(cardId => (deck.id, cardId)))
    (createDeck, createCardsXDeck).mapN((_, _) => deck)
  }

  def addCard(deck: Deck, cardId: Int): doobie.Update0 = {
    sql"""
         insert into superamigos.public.card_x_deck (deck_id, card_id)
         values (${deck.id}, ${cardId})
       """.update
  }

  def select(deckId: Int): Free[connection.ConnectionOp, Option[Deck]] = {
    sql"""
    select id, name
    from superamigos.public.deck
    where id = ${deckId};
  """.query[(Int, String)].option.flatMap {
      case Some((id, name)) => {
        sql"""
        select (card_id)
        from superamigos.public.card_x_deck
        where deck_id = ${deckId}
        """.query[Int].to[List].map(cards => Deck(id, name, cards.toSet).some)
      }
      case _ => ???
    }
  }
}

class DoobieDeckRepository[F[_] : Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends DeckRepository[F] {
  self =>

  import DeckSQL._

  override def create(deck: Deck): F[Deck] = insert(deck).transact(xa)

  override def update(deck: Deck): OptionT[F, Deck] = ???

  override def addCard(deck: Deck, cardId: Int): OptionT[F, Deck] = {
    OptionT(DeckSQL.addCard(deck, cardId).run.map(_ => deck.some).transact(xa))
  }

  override def get(id: Int): OptionT[F, Deck] = OptionT(select(id).transact(xa))

  override def getAll: F[List[Deck]] = selectAll.transact(xa)

  override def delete(id: Int): OptionT[F, Deck] = ???

  override def findByName(name: String): F[Set[Deck]] = ???

  override def list(pageSize: Int, offset: Int): F[List[Deck]] = selectAll.transact(xa)
}

object DoobieDeckRepository {
  def apply[F[_] : Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieDeckRepository[F] =
    new DoobieDeckRepository(xa)
}
