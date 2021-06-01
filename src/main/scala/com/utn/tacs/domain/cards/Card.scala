package com.utn.tacs.domain.cards

final case class Stats(
  height: Int,
  weight: Int,
  intelligence: Int,
  speed: Int,
  power: Int,
  combat: Int,
  strength: Int
) {
  def get(key: String): Int = {
    key match {
      case "height" => height
      case "weight" => weight
      case "intelligence" => intelligence
      case "speed" => speed
      case "power" => power
      case "combat" => combat
      case "strength" => strength
      case _ => ???
    }
  }
}

final case class Biography(fullName: String, publisher: String)

final case class Card(
  id: Int,
  name: String,
  stats: Stats,
  image: String,
  biography: Biography
)
