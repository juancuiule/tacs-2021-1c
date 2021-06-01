package com.utn.tacs.domain.`match`

import io.circe.{Decoder, Encoder, Json}

case class SocketMessage(
  action: SockerAction,
  author: String,
  payload: String,
  timestamp: Option[Long] = None
)

object SocketMessage {
  implicit val encoder = Encoder.instance[SocketMessage](m => Json.obj(
    "action" -> Json.fromString(m.action.toString),
    "author" -> Json.fromString(m.author),
    "payload" -> Json.fromString(m.payload),
    "timestamp" -> Json.fromLong(m.timestamp.getOrElse(0))
  ))

  implicit val decoder = Decoder.instance[SocketMessage](hc =>
    for {
      action <- hc.downField("action").as[String].map(SockerAction.from)
      author <- hc.downField("author").as[String]
      payload <- hc.downField("payload").as[Option[String]]
      timestamp <- hc.downField("timestamp").as[Option[Long]]
    } yield SocketMessage(action, author, payload.getOrElse(""), timestamp)
  )
}

sealed trait SockerAction

object SockerAction {
  def from(in: String): SockerAction = in match {
    case "Join" => Join
    case "Speak" => Speak
    case _ => Unknown
  }

  case object Join extends SockerAction

  case object Speak extends SockerAction

  case object Unknown extends SockerAction
}

