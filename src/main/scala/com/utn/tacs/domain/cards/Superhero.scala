package com.utn.tacs.domain.cards

final case class Powerstats(
                             intelligence: String,
                             strength: String,
                             durability: String,
                             power: String,
                             combat: String,
                             speed: String,
                           )

final case class SuperheroBiography(
                                     `full-name`: String,
                                     publisher: String,
                                   )

final case class Appearance(
                             height: (String, String),
                             weight: (String, String)
                           )

final case class Image(url: String)

final case class Superhero(
                            id: Int,
                            name: String,
                            powerstats: Powerstats,
                            biography: SuperheroBiography,
                            appearance: Appearance,
                            image: Image
                          )