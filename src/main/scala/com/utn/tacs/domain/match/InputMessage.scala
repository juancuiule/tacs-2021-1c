package com.utn.tacs.domain.`match`

import io.circe.Decoder
import io.circe.parser._

trait InputMessage {
  val user: Long
  val theMatch: String
}

case class GetMatch(user: Long, theMatch: String) extends InputMessage

case class Battle(user: Long, theMatch: String, key: String) extends InputMessage

case class Withdraw(user: Long, theMatch: String) extends InputMessage

case class Disconnect(user: Long, theMatch: String) extends InputMessage

case class InvalidInput(user: Long, theMatch: String) extends InputMessage

case class Input(action: String, payload: String)

object InputMessage {
  implicit val decoder: Decoder[Input] = Decoder.instance[Input](hc =>
    for {
      action <- hc.downField("action").as[String]
      payload <- hc.downField("payload").as[Option[String]]
    } yield Input(action, payload.getOrElse("")))

  def _parse(text: String, user: Long, theMatch: String): InputMessage = (for {
    json <- parse(text)
    message <- json.as[Input]
  } yield message)
    .fold(_ => InvalidInput(user, theMatch), input => {
      input.action match {
        case "battle" => (for {
          json <- parse(input.payload)
          payload <- json.as[BattlePayload]
        } yield payload) match {
          case Left(_) => InvalidInput(user, theMatch)
          case Right(bp) => Battle(user, theMatch, bp.key)
        }
        case "getMatch" => GetMatch(user, theMatch)
        case "withdraw" => Withdraw(user, theMatch)
        case _ => InvalidInput(user, theMatch)
      }
    })

  implicit val battlePayloadDecoder: Decoder[BattlePayload] = Decoder.instance[BattlePayload](hc =>
    for {
      key <- hc.downField("key").as[String]
    } yield BattlePayload(key))

  case class BattlePayload(key: String)
}