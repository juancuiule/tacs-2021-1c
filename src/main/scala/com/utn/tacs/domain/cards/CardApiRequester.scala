package com.utn.tacs.domain.cards

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Uri}

import scala.collection.mutable.ListBuffer

object CardRepository {

  // ListBuffer es mutable
  // se argumentar usar lista inmutable
  private val cache: ListBuffer[Card] = new ListBuffer[Card]()

  def get(id: Int): Option[Card] = cache find { aCard => aCard.id == id }

  def getByName(name: String): List[Card] = {
    cache.filter { aCard => aCard.name.toLowerCase contains name.toLowerCase }
      .toList
  }

  def add(card: Card): Unit = cache addOne card

  def addAll(cards: List[Card]): Unit = cache addAll cards

}


case object CardNotFoundError extends Product with Serializable


trait CardApiRequester[F[_]] {
  def getByName(name: String): F[List[Card]]

  def getById(id: Int): F[Card]
}

object CardApiRequester {
  val baseUri = uri"https://superheroapi.com/"

  // TODO: podria ser un metodo, que de una excepcion más legible si no encuentra a api_key
  val uriWithKey: Uri = baseUri.withPath("api/" + scala.util.Properties.envOrElse("SUPERHERO_API_KEY", "") + "/")


  def apply[F[_]](implicit ev: CardApiRequester[F]): CardApiRequester[F] = ev

  def impl[F[_] : Sync](C: Client[F]): CardApiRequester[F] = new CardApiRequester[F] {
    implicit val cardDecoder: EntityDecoder[F, Card] = jsonOf[F, Card]
    implicit val searchDecoder: EntityDecoder[F, SearchResponse] = jsonOf[F, SearchResponse]

    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._


    def getById(id: Int): F[Card] = {
      CardRepository.get(id) match {
        case Some(card) => card.pure[F]
        case None => cardThroughApi(id)
      }
    }

    private def cardThroughApi(id: Int): F[Card] = C.expect[Card](GET(uriWithKey / id.toString)).adaptError({ case t => CardError(t) })



    //         TODO: no se si tiene sentido cachear acá de está forma
    //          porque una vez que ya hay un superheroe con ese nombre
    //          guardado va a traer siempre el que este en caché y no
    //          va a hacer la req a la API
    //          ------------------------------------------------------
    //          RTA: estoy de acuerdo con esto, creo que la busqueda por nombre
    //          se va a tener que mandar siempre a la api, o cachearla de forma especial en un diccionario busquedas = TrieMap[nombreBuscado, List[Card]]


    def getByName(name: String): F[List[Card]] = {
      val cached = CardRepository.getByName(name)
      cached match {
        case Nil => cardThroughApiByName(name)
        case _ => cached.pure[F]
      }
    }

    //    TODO: falta manejar el error que devuelve la API si no
    //     encuentra superheroe con ese nombre
    //     ------------------------------------------------------
    //     RTA: leer CardEndpoints GET -> Root / "test" / "1"

    private def cardThroughApiByName(name: String): F[List[Card]] = C.expect[SearchResponse](GET(uriWithKey / "search/" / name)).adaptError({ case t => CardError(t) }) map { c => c.results }

  }

  final case class SearchResponse(results: List[Card]) extends AnyVal

  final case class CardError(e: Throwable) extends RuntimeException

}