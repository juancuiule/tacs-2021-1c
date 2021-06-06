package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match
import io.circe.generic.auto._
import io.circe.syntax._

trait OutputMessage {
  def forUser(targetUser: Long, inMatch: String): Boolean

  def toString(): String
}

case class SendToUser(user: Long, message: Option[Match]) extends OutputMessage {
  override def forUser(targetUser: Long, inMatch: String): Boolean = user == targetUser && message.exists(_.matchId == inMatch)

  override def toString(): String = message.asJson.toString()
}

case class SendToUsers(users: Set[Long], message: Option[Match]) extends OutputMessage {
  override def forUser(targetUser: Long, inMatch: String): Boolean = users.contains(targetUser) && message.exists(_.matchId == inMatch)

  override def toString(): String = message.asJson.toString()
}