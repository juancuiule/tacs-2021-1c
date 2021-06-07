package com.utn.tacs.domain.`match`

import cats.data.EitherT
import cats.implicits.catsSyntaxApplicativeId
import cats.{Applicative, Monad}
import com.utn.tacs.domain.`match`.Match.MatchStep
import com.utn.tacs.domain.`match`.MatchAction._
import com.utn.tacs.domain.`match`.MatchState
import com.utn.tacs.domain.`match`.MatchState.{BattleResult, Draw, Finished, PreBattle}

import scala.util.Random


trait MatchError extends Serializable with Product

case object MatchNotFoundError extends MatchError

case object MatchAlreadyExistsError extends MatchError


class MatchService[F[+_] : Applicative](
  repository: MatchRepository,
  validation: MatchValidation[F]
)(implicit M: Monad[F]) {
  type ActionMethod = Match => EitherT[F, MatchNotFoundError.type, Match]

  def createMatch(player1: Long, player2: Long, deck: Int): EitherT[F, MatchAlreadyExistsError.type, Match] = {
    val matchId = Random.alphanumeric.take(15).mkString("")
    val newMatch = Match(matchId, deck, player1, player2)
    for {
      _ <- validation.doesNotExist(newMatch)
      createdMatch <- EitherT.liftF(repository.createMatch(newMatch).pure[F])
      initializedMatch <- start(createdMatch.matchId).leftMap(_ => MatchAlreadyExistsError)
    } yield initializedMatch
  }

  private def start(matchId: String): EitherT[F, MatchNotFoundError.type, Match] = {
    for {
      matchInRepo <- getMatch(matchId)
      initializedMatch <- initMatch(matchInRepo)
      matchWithDeckCards <- dealCards(initializedMatch)
    } yield matchWithDeckCards
  }

  def getMatch(matchId: String): EitherT[F, MatchNotFoundError.type, Match] = {
    EitherT.fromOptionF(repository.getMatch(matchId).pure[F], MatchNotFoundError)
  }

  def dealCards: ActionMethod = excecute(MatchAction.DealCards)

  private def initMatch: ActionMethod = m => {
    EitherT.fromEither {
      Right(m.copy(steps = List(
        (InitMatch, BattleResult(List(625, 578, 500, 625, 578, 500, 625, 578, 500, 625, 578, 500), List(), List(), m.player1))
      )))
    }
  }

  def playerCanBattle(aMatch: Match, player: Long): Boolean = {
    aMatch.currentState match {
      case PreBattle(_, _, _, _, _, p) => p == player
      case _ => false
    }
  }

  def noop: ActionMethod = excecute(MatchAction.NoOp)

  def getPlayedRounds(matchId: String): Option[List[MatchStep]] = {
    repository.getMatchRounds(matchId)
  }

  def withdraw(loserPlayer: Long): ActionMethod = excecute(MatchAction.Withdraw(loserPlayer))

  private def excecute(matchAction: MatchAction): Match => EitherT[F, MatchNotFoundError.type, Match] = (aMatch: Match) => {
    val newMatch = play(aMatch, matchAction)
    val updated = repository.updateMatch(newMatch)
    EitherT.fromEither {
      updated match {
        case None => Left(MatchNotFoundError)
        case Some(m) => Right(m)
      }
    }
  }

  private def play(aMatch: Match, action: MatchAction): Match = {
    action match {
      case NoOp => aMatch
      case _ => aMatch.copy(steps = {
        val nextState = nextStateFromAction(aMatch, action)
        aMatch.steps :+ ((action, nextState))
      })
    }
  }

  private def nextStateFromAction(aMatch: Match, action: MatchAction): MatchState = {
    val (_, prevState) = aMatch.steps.last
    (prevState, action) match {
      // TODO: poner cartas del mazo
      case (_: Finished, _) | (_: Draw, _) => prevState
      case (_, NoOp) => prevState
      case (MatchState(cardsInDeck, player1Cards, player2Cards), Withdraw(loser)) =>
        val winner = if (loser.equals(aMatch.player1)) aMatch.player2 else aMatch.player1
        Finished(cardsInDeck, player1Cards, player2Cards, winner)
      case (PreBattle(cardsInDeck, player1Cards, player2Cards, card1, card2, nextToPlay), Battle(_)) =>
        val next = if (nextToPlay == aMatch.player1) aMatch.player2 else aMatch.player1
        BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards, next)
      //        if (card1.stats.get(cardAttribute) > card2.stats.get(cardAttribute)) {
      //          BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards)
      //        } else if (card1.stats.get(cardAttribute) < card2.stats.get(cardAttribute)) {
      //          BattleResult(cardsInDeck, player1Cards, player2Cards :+ card1 :+ card2)
      //        } else {
      //          BattleResult(cardsInDeck, player1Cards :+ card1, player2Cards :+ card2)
      //        }
      case (BattleResult(cards, cards1, cards2, nextToPlay), DealCards) => cards.length match {
        case 0 | 1 =>
          if (cards1.length > cards2.length)
            Finished(cards, cards1, cards2, aMatch.player1)
          else if (cards2.length > cards1.length)
            Finished(cards, cards1, cards2, aMatch.player2)
          else
            Draw(cards, cards1, cards2)
        case _ =>
          val toDeal = cards.take(2)
          val toDeck = cards.drop(2)
          PreBattle(toDeck, cards1, cards2, toDeal.head, toDeal.last, nextToPlay)
      }
      case _ => prevState
    }
  }

  def battleByAttribute(attribute: String): ActionMethod = excecute(MatchAction.Battle(attribute)) andThen (_.flatMap(dealCards))
}

object MatchService {
  def apply[F[+_] : Applicative](repository: MatchRepository, validation: MatchValidation[F])(implicit M: Monad[F]) =
    new MatchService[F](repository, validation)
}