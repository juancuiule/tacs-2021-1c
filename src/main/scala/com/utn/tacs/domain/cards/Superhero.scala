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
                          ) {

  private def isValidField(fieldData: String): Boolean = !List("null", "0 kg", "0 cm", "-").contains(fieldData)


  private def canBeCard: Boolean = {
    List(
      powerstats.intelligence,
      powerstats.strength,
      powerstats.speed,
      powerstats.power,
      powerstats.strength,
      appearance.height._2,
      appearance.weight._2
    ) forall isValidField
  }

  def card: Option[Card] = {
    if (!canBeCard)
      None
    else {
      val cardStats = Stats(
        height = appearance.height._2.replace(" cm", "").toInt,
        weight = if (appearance.weight._2.contains("tons")) appearance.weight._2.replace(" tons", "").toInt * 1000 else appearance.weight._2.replace(" kg", "").toInt,
        intelligence = powerstats.intelligence.toInt,
        speed = powerstats.speed.toInt,
        power = powerstats.power.toInt,
        combat = powerstats.combat.toInt,
        strength = powerstats.strength.toInt
      )
      val cardBiography = Biography(fullName = biography.`full-name`, publisher = biography.publisher)
      Some(Card(id = id, name = name, stats = cardStats, image = image.url, biography = cardBiography))
    }
  }
}