package com.utn.tacs.domain.`match`

import cats.data.EitherT
import cats.implicits.catsSyntaxApplicativeId
import cats.{Applicative, Monad}
import com.utn.tacs.domain.`match`.Match.MatchStep
import com.utn.tacs.domain.`match`.MatchAction._
import com.utn.tacs.domain.`match`.MatchState.{BattleResult, Draw, Finished, PreBattle}

import scala.util.Random


trait MatchError extends Serializable with Product

case object MatchNotFoundError extends MatchError

case object MatchAlreadyExistsError extends MatchError


class MatchService[F[+_] : Applicative](
  repository: MatchRepository,
  validation: MatchValidation[F]
) {
  type ActionMethod = Match => EitherT[F, MatchNotFoundError.type, Match]

  def createMatch(player1: Long, player2: Long, deck: Int)(implicit M: Monad[F]): EitherT[F, MatchAlreadyExistsError.type, Match] = {
    val matchId = Random.alphanumeric.take(15).mkString("")
    println(matchId)
    val newMatch = Match(matchId, deck, player1, player2)
    for {
      _ <- validation.doesNotExist(newMatch)
      createdMatch <- EitherT.liftF(repository.createMatch(newMatch).pure[F])
      initializedMatch <- start(createdMatch.matchId).leftMap(_ => MatchAlreadyExistsError)
    } yield initializedMatch
  }

  def start(matchId: String)(implicit M: Monad[F]): EitherT[F, MatchNotFoundError.type, Match] = {
    for {
      matchInRepo <- getMatch(matchId)
      initializedMatch <- initMatch(matchInRepo)
      matchWithDeckCards <- dealCards(initializedMatch)
    } yield (matchWithDeckCards)
  }

  def getMatch(matchId: String): EitherT[F, MatchNotFoundError.type, Match] = {
    EitherT.fromOptionF(repository.getMatch(matchId).pure[F], MatchNotFoundError)
  }

  def dealCards: ActionMethod = excecute(MatchAction.DealCards)

  def excecute(matchAction: MatchAction): Match => EitherT[F, MatchNotFoundError.type, Match] = (aMatch: Match) => {
    val newMatch = play(aMatch, matchAction)
    val updated = repository.updateMatch(newMatch)
    EitherT.fromEither {
      updated match {
        case None => Left(MatchNotFoundError)
        case Some(m) => Right(m)
      }
    }
  }

  def play(aMatch: Match, action: MatchAction): Match = {
    aMatch.copy(steps = {
      val nextState = nextStateFromAction(aMatch, action)
      aMatch.steps :+ ((action, nextState))
    })
  }

  def nextStateFromAction(aMatch: Match, action: MatchAction): MatchState = {
    val (_, prevState) = aMatch.steps.last
    (prevState, action) match {
      // TODO: poner cartas del mazo
      case (_: Finished, _) | (_: Draw, _) => prevState
      case (_, NoOp) => prevState
      case (_, Withdraw(loser)) =>
        val winner = if (loser.equals(aMatch.player1)) aMatch.player2 else aMatch.player1
        Finished(winner)
      case (PreBattle(cardsInDeck, player1Cards, player2Cards, card1, card2), Battle(_)) =>
        BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards)
      //        if (card1.stats.get(cardAttribute) > card2.stats.get(cardAttribute)) {
      //          BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards)
      //        } else if (card1.stats.get(cardAttribute) < card2.stats.get(cardAttribute)) {
      //          BattleResult(cardsInDeck, player1Cards, player2Cards :+ card1 :+ card2)
      //        } else {
      //          BattleResult(cardsInDeck, player1Cards :+ card1, player2Cards :+ card2)
      //        }
      case (BattleResult(cards, cards1, cards2), DealCards) => {
        cards.length match {
          case 0 | 1 =>
            if (cards1.length > cards2.length)
              Finished(aMatch.player1)
            else if (cards2.length > cards1.length)
              Finished(aMatch.player2)
            else
              Draw(cards, cards1, cards2)
          case _ =>
            val toDeal = cards.take(2)
            val toDeck = cards.drop(2)
            PreBattle(toDeck, cards1, cards2, toDeal.head, toDeal.last)
        }
      }
      case _ => prevState
    }
  }

  def initMatch: ActionMethod = m => {
    EitherT.fromEither {
      Right(m.copy(steps = List(
        (InitMatch, BattleResult(List(1, 2, 3, 4, 5), List(), List()))
      )))
    }
  }

  def noop: ActionMethod = excecute(MatchAction.NoOp)

  def getPlayedRounds(matchId: String): Option[List[MatchStep]] = {
    repository.getMatchRounds(matchId)
  }

  def withdraw(loserPlayer: Long): ActionMethod = excecute(MatchAction.Withdraw(loserPlayer))

  def battleByAttribute(attribute: String)(implicit M: Monad[F]): ActionMethod = excecute(MatchAction.Battle(attribute)) andThen (_.flatMap(dealCards))
}

object MatchService {
  def apply[F[+_] : Applicative](repository: MatchRepository, validation: MatchValidation[F]) =
    new MatchService[F](repository, validation)
}