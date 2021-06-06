package com.utn.tacs.domain.`match`

trait OutputMessage {
  def forUser(targetUser: Long): Boolean
}

case class WelcomeUser(user: Long) extends OutputMessage {
  override def forUser(targetUser: Long): Boolean = user == targetUser
}

case class SendToUser(user: Long, message: Any) extends OutputMessage {
  override def forUser(targetUser: Long): Boolean = user == targetUser
}

case class SendToUsers(users: Set[Long], message: Any) extends OutputMessage {
  override def forUser(targetUser: Long): Boolean = users.contains(targetUser)
}

case object KeepAlive extends OutputMessage {
  override def forUser(targetUser: Long): Boolean = true
}