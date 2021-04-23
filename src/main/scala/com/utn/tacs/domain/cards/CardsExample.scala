package com.utn.tacs.domain.cards

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Functor, Monad}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._

import scala.collection.concurrent.TrieMap
import scala.util.Random

// EitherT https://typelevel.org/cats/datatypes/eithert.html
// Either https://typelevel.org/cats/datatypes/either.html
// Either[A, B] = Left A | Right B

// Model
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
                       id: Option[Int] = None,
                       name: String,
                       powerstats: PowerStats,
                       image: Image,
                       biography: Option[Biography] = None
                     )

//

// Errors
trait CardError extends Serializable with Product

case class CardAlreadyExistsError(card: Card) extends CardError

case object CardNotFoundError extends CardError

//

// Repository
class CardRepository[F[_] : Applicative] {
  private val cache = new TrieMap[Int, Card]()
  private val random = new Random()

  def create(card: Card): F[Card] = {
    val id = random.nextInt()
    val toSave = card.copy(id = id.some)
    cache addOne (id -> toSave)
    toSave.pure[F]
  }

  def update(card: Card): F[Option[Card]] = ???

  def get(id: Int): F[Option[Card]] = cache.get(id).pure[F]

  def delete(id: Int): F[Option[Card]] = ???

  def findByName(name: String): F[Set[Card]] =
    cache.values.filter(card => card.name == name).toSet.pure[F]

  def list(pageSize: Int, offset: Int): F[List[Card]] = cache.values.slice(offset, offset + pageSize).toList.pure[F]
}

object CardRepository {
  def apply[F[_] : Applicative]() = new CardRepository[F]()
}

//

// Validation utils
class CardValidation[F[_] : Applicative](repository: CardRepository[F]) {
  // Sirve para buscar si la carta existe
  def doesNotExist(card: Card): EitherT[F, CardAlreadyExistsError, Unit] = EitherT {
    // busca por nombre, como devuelve un F[Set[Card]] usa map para transformar el Set[Card]
    // (como se usa con Option para transformar el valor)
    // si todas las cartas con el mismo nombre tienen la distinta bio entonces devuelve un Right(())
    // si alguna coincide devuelve un Left con el error
    repository.findByName(card.name).map {
      cards =>
        if (cards.forall(c => c.biography != card.biography))
          Right(())
        else
          Left(CardAlreadyExistsError(card))
    }
    //                     v funcion que transforma en B
    // map[A, B](fa: F[A])(f: A => B): F[B]
    //           ^ resultado del findByName
    // A = Set[Card]
    // fa: F[Set[Card]]
    // f: Set[Card] => B
    // B = Either[CardAlreadyExistsError, Unit]
  }

  def exists(cardId: Option[Int]): EitherT[F, CardNotFoundError.type, Unit] = EitherT {
    cardId match {
      // map[A, B](fa: F[A])(f: A => B): F[B]
      // A: Option[Int]
      // B: Either[CardNotFoundError.type, Unit]
      case Some(id) => repository.get(id).map {
        case Some(_) => Right(())
        case _ => Left(CardNotFoundError)
      }
      case _ => Either.left[CardNotFoundError.type, Unit](CardNotFoundError).pure[F]
    }
  }
}

object CardValidation {
  def apply[F[_] : Applicative](repository: CardRepository[F]) = new CardValidation[F](repository)
}

//


// Service

// sealed indica que _todo lo que implemente este trait va a estar en este archivo
// ayuda a indicar si un pattern matching no está teniendo en cuenta todas las opciones
sealed trait ExternalApiResponse
final case class SearchResponse(results: List[Card]) extends ExternalApiResponse
final case class ApiResponseError(response: String, error: String) extends ExternalApiResponse

// El + es medio complicado de explicar pero es para establecer que si existe relación
// entre dos _ entonces existe la misma relacion entre dos F[_]

// En este caso:
// ApiResponseError <: ExternalApiResponse
// y por ser + (covariante, va en el mismo sentido), entonces:
// F[ApiResponseError] <: F[ExternalApiResponse]
class CardService[F[+_]](
                          repository: CardRepository[F],
                          validation: CardValidation[F],
                          C: Client[F]
                        ) {
  val baseUri = uri"https://superheroapi.com/"
  val uriWithKey: Uri = baseUri.withPath("api/" + scala.util.Properties.envOrElse("SUPERHERO_API_KEY", "") + "/")


  def create(card: Card)(implicit M: Monad[F]): EitherT[F, CardAlreadyExistsError, Card] =
    for {
      _ <- validation.doesNotExist(card)
      saved <- EitherT.liftF(repository.create(card))
    } yield saved

  def getAll(pageSize: Int, offset: Int): F[List[Card]] =
    repository.list(pageSize, offset)

  def get(id: Int)(implicit FF: Functor[F]): EitherT[F, CardNotFoundError.type, Card] =
    EitherT.fromOptionF(repository.get(id), CardNotFoundError)

  def getByName(name: String): F[Set[Card]] = repository.findByName(name)

  def list(pageSize: Int, offset: Int): F[List[Card]] = repository.list(pageSize, offset)

  // TODO: revisar como podemos separar el service cartas del service que le pega a la API
  //       o ver si tiene sentido hacer eso
  def getCardsFromAPI(searchName: String)(implicit FF: Sync[F]): F[Either[String, ExternalApiResponse]] = {
    implicit val searchDecoder: EntityDecoder[F, SearchResponse] = jsonOf[F, SearchResponse]
    implicit val apiResponseErrorDecoder: EntityDecoder[F, ApiResponseError] = jsonOf[F, ApiResponseError]

    C.get(uriWithKey / "search/" / searchName) {
      // La api de superheroes devuelve un 200 aún cuando no encontro superhero
      case r@Response(Status.Ok, _, _, _, _) => {
        // val x: DecodeResult[F, SearchResponse] = r.attemptAs[SearchResponse]
        // attemptAs[UnTipo] devuelve un EitherT[F, DecodeFailure, UnTipo]
        // left{map,flatMap} hace ese map si el either es un left, si hay error: DecodeFailure
        // en este caso flatMap porque devolvemos otro DecodeResult[F, UnTipo]
        // leftMap :: (f: DecodeFailure => OtroTipo)
        // leftFlatMap :: (f: DecodeFailure => DecodeResult[F, UnTipo])
        r.attemptAs[SearchResponse].leftFlatMap[ExternalApiResponse, String](_ => EitherT {
          // si lo de arriba falló en parsearse entonces la respuesta no tenía la forma esperada
          // probablemente sea algo como { response: String, message: String }
          r.attemptAs[ApiResponseError].leftMap(_ => "Error de parseo").value
        })
      }.value
      case _ => Either.left[String, ExternalApiResponse]("match error").pure[F]
    }
  }
}

object CardService {
  def apply[F[+_]](repository: CardRepository[F], validation: CardValidation[F], client: Client[F]) =
    new CardService[F](repository, validation, client)
}

//

// Endpoint
class CardEndpoints[F[+_] : Sync](client: Client[F]) extends Http4sDsl[F] {

  // Esto probablemente convenga instanciarlo desde el `Server`
  val repository: CardRepository[F] = CardRepository()
  val validator: CardValidation[F] = CardValidation(repository)
  val service: CardService[F] = CardService(repository, validator, client)

  implicit val cardDecoder: EntityDecoder[F, Card] = jsonOf
  val getCardEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / IntVar(id) =>
        // repository.get(id) :: F[Option[Card]]
        // map :: Option[Card] => B
        // flatMap :: Option[Card] => F[B]
        // es flatMap porque no devuelvo una carta
        repository.get(id).flatMap {
          case Some(card) => Ok(card.asJson)
          case None => NotFound()
        }
    }

  def endpoints(): HttpRoutes[F] =
    superHeroApiEndpoint <+>
      createCardEndpoint <+>
      getCardEndpoint <+>
      getAllEndpoint

  def superHeroApiEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "search" / searchName =>
        val actionResult = service.getCardsFromAPI(searchName)
        actionResult.flatMap {
          case Right(cards: SearchResponse) => Ok(cards.asJson)
          case Right(resp: ApiResponseError) => NotFound(resp.asJson)
          case Left(e) => InternalServerError(e)
        }
    }

  def createCardEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req@POST -> Root =>
        val actionResult = for {
          card <- req.as[Card]
          result <- service.create(card).value
        } yield result

        actionResult.flatMap {
          case Right(card) => Ok(card.asJson)
          case Left(CardAlreadyExistsError(card)) => Conflict(card.asJson)
        }
    }

  def getAllEndpoint: HttpRoutes[F] = {
    object PageSizeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("pagesize")
    object OffsetQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")
    HttpRoutes.of[F] {
      case GET -> Root :? PageSizeQueryParamMatcher(pageSize) +& OffsetQueryParamMatcher(offset) =>
        for {
          cards <- service.getAll(pageSize.getOrElse(100), offset.getOrElse(0))
          resp <- Ok(Json.obj(
            ("cards", cards.asJson)
          ))
        } yield resp
    }
  }
}

object CardEndpoints {
  def apply[F[+_] : Sync](client: Client[F]): HttpRoutes[F] =
    new CardEndpoints[F](client).endpoints()
}

// Así funcionan las cosas como el "asJson" (https://blog.rockthejvm.com/scala-3-extension-methods/)
// object Prueba {
//   case class Person(name: String) {
//     def greet: String = s"Hi, I'm $name, nice to meet you."
//   }
//
//   implicit class PersonLike(string: String) {
//     def greet: String = Person(string).greet
//   }
//
//   "Juan".greet // Hi, I'm Juan, nice to meet you
// }