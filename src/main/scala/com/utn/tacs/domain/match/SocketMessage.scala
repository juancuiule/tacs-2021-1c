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

  def parsePayload(): Either[circe.Error, MatchAction] = {
    action match {
      case Withdraw => getAction[MatchAction.Withdraw](payload)
      case Battle => getAction[MatchAction.Battle](payload)
      case Unknown | GetMatch => Right(MatchAction.NoOp)
    }
  }

  def getAction[T <: MatchAction](payload: String)(implicit D: Decoder[T]): Either[circe.Error, T] = {
    (for {
      json <- parse(payload)
      action <- json.as[T]
    } yield action)
  }
}

object SocketMessage {
  implicit val encoder: Encoder[SocketMessage] = Encoder.instance[SocketMessage](m => Json.obj(
    "action" -> Json.fromString(m.action.toString),
    "payload" -> Json.fromString(m.payload),
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
    case "get" => GetMatch
    case _ => Unknown
  }

  case object Unknown extends SocketAction
  case object GetMatch extends SocketAction

  case object Withdraw extends SocketAction

  case object Battle extends SocketAction
}