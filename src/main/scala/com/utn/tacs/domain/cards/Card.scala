package com.utn.tacs.domain.cards

final case class PowerStats(
                             intelligence: Int,
                             strength: Int,
                             speed: Int,
                             durability: Int,
                             power: Int,
                             combat: Int
                           )

final case class Biography(`full-name`: String, publisher: String)

final case class Appearance(gender: String, race: String)

final case class Image(url: String)

final case class Card(
                       id: Int,
                       name: String,
                       powerstats: PowerStats,
                       image: Image,
                       biography: Option[Biography] = None
                     )