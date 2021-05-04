package com.utn.tacs.domain.`match`

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import com.utn.tacs.infrastructure.repository.{MatchNotFoundError, MatchRepository}

import scala.util.Random

trait MatchService[F[_]] {
  def createMatch(player1Id: String, player2Id: String, deckId: String): F[Match]

  def getMatch(matchId: String): Option[Match]

  def withdraw(matchId: String, loserPlayer: String): Either[MatchError, Match]

  def getPlayedRounds(matchId: String): List[Round]
}


object MatchService {

  private val repo: MatchRepository = MatchRepository

  def impl[F[_] : Applicative]: MatchService[F] = new MatchService[F] {

    def createMatch(player1Id: String, player2Id: String, deckId: String): F[Match] = {
      val matchId = Random.alphanumeric.take(15).mkString("")
      val newMatch = Match(matchId, player1Id, player2Id, deckId, "active", "no_winner_yet")
      repo.createMatch(newMatch).pure[F]
    }

    override def getMatch(matchId: String): Option[Match] = {
      repo.getMatch(matchId)
    }

    override def withdraw(matchId: String, loserPlayer: String): Either[MatchError, Match] = {
      repo.getMatch(matchId)
        .map(m => {
          m.status = "finished"
          if (m.player1Id.equals(loserPlayer)) {
            m.winnerId = m.player2Id
          } else {
            m.winnerId = m.player1Id
          }
          val updateResult = repo.updateMatch(m)
          updateResult match {
            case Left(e) => Left(e)
            case Right(v) => Right(v)
          }
        }).getOrElse(Left(MatchNotFoundError))
    }

    override def getPlayedRounds(matchId: String): List[Round] = {
      repo.getMatchRounds(matchId)
    }
  }
}
