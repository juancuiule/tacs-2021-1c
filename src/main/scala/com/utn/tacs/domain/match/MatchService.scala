package com.utn.tacs.domain.`match`

import cats.Applicative
import cats.data.{EitherT, OptionT}
import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.all._
import com.utn.tacs.domain.`match`.Match.MatchStep
import com.utn.tacs.domain.`match`.MatchAction._
import com.utn.tacs.domain.`match`.MatchState
import com.utn.tacs.domain.`match`.MatchState.{BattleResult, Draw, Finished, PreBattle}
import com.utn.tacs.domain.cards.CardService
import com.utn.tacs.domain.deck.DeckService

import scala.util.Random


trait MatchError extends Serializable with Product

case object MatchNotFoundError extends MatchError

case object MatchAlreadyExistsError extends MatchError

case object MatchActionError extends MatchError


class MatchService[F[+_] : Applicative](
  repository: MatchRepository[F],
  validation: MatchValidation[F],
  deckService: DeckService[F],
  cardService: CardService[F]
)(implicit FF: Sync[F]) {
  type ActionMethod = Match => EitherT[F, MatchError, Match]

  def createMatch(player1: Long, player2: Long, deck: Int): EitherT[F, MatchAlreadyExistsError.type, Match] = {
    val matchId = Random.alphanumeric.take(15).mkString("")
    val newMatch = Match(matchId, deck, player1, player2)
    for {
      _ <- validation.doesNotExist(newMatch)
      createdMatch <- EitherT.liftF(repository.createMatch(newMatch))
      initializedMatch <- start(createdMatch.matchId).leftMap(_ => MatchAlreadyExistsError)
      theMatch <- EitherT.liftF(repository.updateMatch(initializedMatch).value)
    } yield theMatch.get
  }

  private def start(matchId: String): EitherT[F, MatchError, Match] = {
    for {
      matchInRepo <- getMatch(matchId)
      initializedMatch <- initMatch(matchInRepo)
      matchWithDeckCards <- dealCards(initializedMatch)
    } yield matchWithDeckCards
  }

  def getMatch(matchId: String): EitherT[F, MatchNotFoundError.type, Match] = {
    repository.getMatch(matchId)
  }

  def dealCards: ActionMethod = excecute(MatchAction.DealCards)

  private def excecute(matchAction: MatchAction): ActionMethod = (aMatch: Match) => {
    for {
      m <- play(aMatch, matchAction)
      um <- EitherT.liftF(repository.updateMatch(m).value)
    } yield um.get
  }

  private def play(aMatch: Match, action: MatchAction)(implicit FF: Sync[F]): EitherT[F, MatchActionError.type, Match] = {
    val nextState = nextStateFromAction(aMatch, action)

    action match {
      case NoOp => EitherT.right(aMatch.pure[F])
      case _ => EitherT.right(nextState.value.flatMap {
        case Left(_) => aMatch.pure[F]
        case Right(ns) => aMatch.copy(steps = {
          aMatch.steps :+ ((action, ns))
        }).pure[F]
      })
    }
  }

  private def nextStateFromAction(aMatch: Match, action: MatchAction)(implicit FF: Sync[F]): EitherT[F, MatchActionError.type, MatchState] = {
    val (_, prevState) = aMatch.steps.last
    EitherT.right(
      (prevState, action) match {
        // TODO: poner cartas del mazo
        case (_: Finished, _) | (_: Draw, _) => prevState.pure[F]
        case (_, NoOp) => prevState.pure[F]
        case (MatchState(cardsInDeck, player1Cards, player2Cards), Withdraw(loser)) =>
          val winner = if (loser.equals(aMatch.player1)) aMatch.player2 else aMatch.player1
          Finished(cardsInDeck, player1Cards, player2Cards, winner).pure[F]
        case (PreBattle(cardsInDeck, player1Cards, player2Cards, card1, card2, nextToPlay), Battle(cardAttribute)) =>
          val next = if (nextToPlay == aMatch.player1) aMatch.player2 else aMatch.player1
          BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards, next)
          val cards = for {
            c1 <- cardService.get(card1)
            c2 <- cardService.get(card2)
          } yield (c1, c2)

          val op = (key: String) => (a: Int, b: Int) => {
            if (key == "weight") (a < b) else (a > b)
          }

          cards.value.flatMap {
            case Right((c1, c2)) => {
              val card1Value = c1.stats.get(cardAttribute)
              val card2Value = c2.stats.get(cardAttribute)

              if (op(cardAttribute)(card1Value, card2Value)) {
                BattleResult(cardsInDeck, player1Cards :+ card1 :+ card2, player2Cards, next).pure[F]
              } else if (op(cardAttribute)(card2Value, card1Value)) {
                BattleResult(cardsInDeck, player1Cards, player2Cards :+ card1 :+ card2, next).pure[F]
              } else {
                BattleResult(cardsInDeck, player1Cards :+ card1, player2Cards :+ card2, next).pure[F]
              }
            }
            case Left(_) => prevState.pure[F]
          }

        case (BattleResult(cards, cards1, cards2, nextToPlay), DealCards) => cards.length match {
          case 0 | 1 =>
            if (cards1.length > cards2.length)
              Finished(cards, cards1, cards2, aMatch.player1).pure[F]
            else if (cards2.length > cards1.length)
              Finished(cards, cards1, cards2, aMatch.player2).pure[F]
            else
              Draw(cards, cards1, cards2).pure[F]
          case _ =>
            val toDeal = cards.take(2)
            val toDeck = cards.drop(2)
            PreBattle(toDeck, cards1, cards2, toDeal.head, toDeal.last, nextToPlay).pure[F]
        }
        case _ => prevState.pure[F]
      }
    )
  }

  private def initMatch: ActionMethod = m => {
    val cards = deckService.getDeckCards(m.deck).getOrElse(List())
    val theMatch: F[Match] = cards.map(deckCards => {
      m.copy(steps = List(
        (InitMatch, BattleResult(deckCards, List(), List(), m.player1))
      ))
    })
    EitherT.right(theMatch)
  }

  def getMatchesForPlayer(player: Long): F[List[Match]] = {
    for {
      matches <- repository.getAll
    } yield matches.filter(_.hasPlayer(player))
  }

  def getMatches: F[List[Match]] = repository.getAll

  def playerCanBattle(aMatch: Match, player: Long): Boolean = {
    aMatch.currentState match {
      case PreBattle(_, _, _, _, _, p) => p == player
      case _ => false
    }
  }

  def noop: ActionMethod = excecute(MatchAction.NoOp)

  def getPlayedRounds(matchId: String): OptionT[F, List[MatchStep]] = {
    repository.getMatchRounds(matchId)
  }

  def withdraw(loserPlayer: Long): ActionMethod = excecute(MatchAction.Withdraw(loserPlayer))

  def battleByAttribute(attribute: String): ActionMethod = excecute(MatchAction.Battle(attribute)) andThen (_.flatMap(dealCards))
}

object MatchService {
  def apply[F[+_] : Applicative](repository: MatchRepository[F], validation: MatchValidation[F], deckService: DeckService[F], cardService: CardService[F])(implicit FF: Sync[F]) =
    new MatchService[F](repository, validation, deckService, cardService)
}