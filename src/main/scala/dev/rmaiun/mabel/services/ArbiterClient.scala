package dev.rmaiun.mabel.services

import dev.rmaiun.mabel.dtos.ArbiterDto.codec._
import dev.rmaiun.mabel.dtos.ArbiterDto.{GetRealmDtoIn, GetRealmDtoOut, RegisterUserDtoIn, RegisterUserDtoOut}
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder}
import zio.{Task, _}
import zio.interop.catz._

object ArbiterClient {
  type HasArbiterClient = Has[ArbiterClient.Service]

  trait Service {
    def findRealm(dtoIn: GetRealmDtoIn): Task[GetRealmDtoOut]
    def addPlayer(dtoIn: RegisterUserDtoIn): Task[RegisterUserDtoOut]
  }

  case class ArbiterClientService(client: Client[Task]) extends Service {
    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[Task, A] = jsonOf[Task, A]
    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

    override def findRealm(dtoIn: GetRealmDtoIn): Task[GetRealmDtoOut] =
      client.expect[GetRealmDtoOut](uri"http://localhost:9091/realms/find")

    override def addPlayer(dtoIn: RegisterUserDtoIn): Task[RegisterUserDtoOut] =
      client.expect[RegisterUserDtoOut](uri"http://localhost:9091/users/register")

  }

  val live: URLayer[Has[Client[Task]], HasArbiterClient] =
    (ArbiterClientService(_)).toLayer
}
