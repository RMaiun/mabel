package dev.rmaiun.mabel

import cats.Monad
import cats.effect.Concurrent
import dev.rmaiun.mabel.Jokes.Bla
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, Json}
import org.http4s.Method._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import cats.implicits._
trait Jokes[F[_]] {
  def get: F[Bla]
}

object Jokes {
  def apply[F[_]](implicit ev: Jokes[F]): Jokes[F] = ev

  final case class Joke(joke: String) extends AnyVal
  implicit val jokeDecoder: Decoder[Joke] = deriveDecoder[Joke]
  implicit def jokeEntityDecoder[F[_]: Concurrent,T](implicit decoder: Decoder[T]): EntityDecoder[F, T] =
    jsonOf
  implicit val jokeEncoder: Encoder[Joke] = deriveEncoder[Joke]
  implicit def jokeEntityEncoder[F[_],T](implicit encoder: Encoder[T]): EntityEncoder[F, T] =
    jsonEncoderOf

  case class Bla(joke:String)
  implicit val blaDecoder: Decoder[Bla] = deriveDecoder[Bla]
  implicit val blaEncoder: Encoder[Bla] = deriveEncoder[Bla]


  final case class JokeError(e: Throwable) extends RuntimeException

  def impl[F[_]:Monad: Concurrent](C: Client[F]): Jokes[F] = new Jokes[F] {
    val dsl = new Http4sClientDsl[F] {}
    import dsl._
    import org.http4s.headers._
    val request: Request[F] = Request[F](
      GET,
      uri"https://icanhazdadjoke.com/",
      headers = Headers(
        Accept(MediaType.application.json),
        "x" -> "y"
      )
    )
    def get: F[Bla] = {
      C.expect[Bla](request)
    }
  }
}
