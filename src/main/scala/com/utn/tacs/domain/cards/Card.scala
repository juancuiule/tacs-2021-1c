package com.utn.tacs.domain.cards

final case class PowerStats(
  intelligence: Int,
  strength: Int,
  speed: Int,
  durability: Int,
  power: Int,
  combat: Int
)

final case class Biography(fullName: String, publisher: String)

final case class Image(url: String)

final case class Card(
  id: Option[Int] = None,
  name: String,
  powerstats: Option[PowerStats] = None,
  image: Image,
  biography: Option[Biography] = None
)