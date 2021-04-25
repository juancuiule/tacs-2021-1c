package com.utn.tacs.domain.cards

final case class Stats(
                        height: Int,
                        weight: Int,
                        intelligence: Int,
                        speed: Int,
                        power: Int,
                        combat: Int,
                        strength: Int,
                      )


final case class Biography(fullName: String, publisher: String)


final case class Card(
                       id: Option[Int] = None,
                       name: String,
                       stats: Option[Stats] = None,
                       image: String,
                       biography: Option[Biography] = None
                     )