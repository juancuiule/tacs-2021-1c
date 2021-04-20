package com.utn.tacs.domain.cards

final case class PowerStats(intelligence: String,
                            strength: String,
                            speed: String,
                            durability: String,
                            power: String,
                            combat: String
                           )

final case class Image(url: String)

final case class Card(id: String, name: String, powerstats: PowerStats, image: Image)