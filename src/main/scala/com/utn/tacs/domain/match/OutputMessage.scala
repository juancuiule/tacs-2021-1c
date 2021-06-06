package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match
import io.circe.generic.auto._
import io.circe.syntax._

trait OutputMessage {
  def forUser(targetUser: Long): Boolean

  def toString(): String
}

case class SendToUser(user: Long, message: Option[Match]) extends OutputMessage {
  override def forUser(targetUser: Long): Boolean = user == targetUser

  override def toString(): String = message.asJson.toString()
}

case class SendToUsers(users: Set[Long], message: Option[Match]) extends OutputMessage {
  override def forUser(targetUser: Long): Boolean = users.contains(targetUser)

  override def toString(): String = message.asJson.toString()
}