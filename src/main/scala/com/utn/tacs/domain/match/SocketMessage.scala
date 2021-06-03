package com.utn.tacs.domain.`match`

import io.circe
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.{Decoder, Encoder, Json}

case class SocketMessage(
  matchId: String,
  author: String,
  action: SocketAction,
  payload: String,
  timestamp: Option[Long] = None
) {
  import SocketAction._

  def getAction[T <: MatchAction](payload: String)(implicit D: Decoder[T]): Either[circe.Error, T] = {
    (for {
      json <- parse(payload)
      action <- json.as[T]
    } yield action)
  }


  def _parse(): Either[circe.Error, MatchAction] = {
    action match {
      case Withdraw => getAction[MatchAction.Withdraw](payload)
      case DealCards => Right(MatchAction.DealCards)
      case Battle => getAction[MatchAction.Battle](payload)
      case Unknown => Right(MatchAction.NoOp)
    }
  }
}

object SocketMessage {
  implicit val encoder: Encoder[SocketMessage] = Encoder.instance[SocketMessage](m => Json.obj(
    "action" -> Json.fromString(m.action.toString),
    "author" -> Json.fromString(m.author),
    "payload" -> Json.fromString(m.payload),
    "timestamp" -> Json.fromLong(m.timestamp.getOrElse(0))
  ))

  implicit val decoder: Decoder[SocketMessage] = Decoder.instance[SocketMessage](hc =>
    for {
      matchId <- hc.downField("matchId").as[String]
      action <- hc.downField("action").as[String].map(SocketAction.from)
      author <- hc.downField("author").as[String]
      payload <- hc.downField("payload").as[Option[String]]
      timestamp <- hc.downField("timestamp").as[Option[Long]]
    } yield SocketMessage(matchId, author, action, payload.getOrElse(""), timestamp)
  )
}

sealed trait SocketAction

object SocketAction {
  def from(in: String): SocketAction = in match {
    case "withdraw" => Withdraw
    case "battle" => Battle
    case "dealCards" => DealCards
    case _ => Unknown
  }

  case object Unknown extends SocketAction
  case object Withdraw extends SocketAction
  case object Battle extends SocketAction
  case object DealCards extends SocketAction
}