package com.utn.tacs.domain.user

import cats._
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

final case class Role(roleName: String)

object Role extends SimpleAuthEnum[Role, String] {
  val Admin: Role = Role("Admin")
  val Player: Role = Role("Player")

  override val values: AuthGroup[Role] = AuthGroup(Admin, Player)

  override def getRepr(t: Role): String = t.roleName

  implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals[Role]
}