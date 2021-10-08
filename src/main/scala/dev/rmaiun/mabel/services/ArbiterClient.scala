package dev.rmaiun.mabel.services

import dev.rmaiun.mabel.dtos.ArbiterDto._
import dev.rmaiun.mabel.dtos.ArbiterDto.codec._
import io.circe.{ Decoder, Encoder }
import org.http4s.Method.POST
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{ EntityDecoder, EntityEncoder, Request }
import zio.interop.catz._
import zio.{ Task, _ }

object ArbiterClient {
  type HasArbiterClient = Has[ArbiterClient.Service]

  trait Service {
    def findRealm(realm: String): Task[GetRealmDtoOut]
    def findPlayer(surname: String): Task[FindUserDtoOut]
    def addPlayer(dtoIn: RegisterUserDtoIn): Task[RegisterUserDtoOut]
    def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Task[AssignUserToRealmDtoOut]
    def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Task[AddGameHistoryDtoOut]
    def storeEloPoints(dtoIn: AddEloPointsDtoIn): Task[AddEloPointsDtoOut]
    def listGameHistory(realm: String, season: String): Task[ListGameHistoryDtoOut]
    def listCalculatedEloPoints(users: List[String]): Task[ListEloPointsDtoOut]
  }

  case class ArbiterClientService(client: Client[Task]) extends Service {
    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[Task, A] = jsonOf[Task, A]
    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[Task, A] = jsonEncoderOf[Task, A]
    val baseUri                                                       = uri"http://localhost:9091"

    override def findRealm(realm: String): Task[GetRealmDtoOut] = {
      val uri = baseUri / "realms" / "find" / s"$realm"
      client.expect[GetRealmDtoOut](uri)
    }

    override def addPlayer(dtoIn: RegisterUserDtoIn): Task[RegisterUserDtoOut] = {
      val uri     = baseUri / "users" / "register"
      val request = Request[Task](POST, uri).withEntity(dtoIn)
      client.expect[RegisterUserDtoOut](request)
    }

    override def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Task[AssignUserToRealmDtoOut] = {
      val uri     = baseUri / "users" / "assignToRealm"
      val request = Request[Task](POST, uri).withEntity(dtoIn)
      client.expect[AssignUserToRealmDtoOut](request)
    }

    override def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Task[AddGameHistoryDtoOut] = {
      val uri     = baseUri / "games" / "history" / "store"
      val request = Request[Task](POST, uri).withEntity(dtoIn)
      client.expect[AddGameHistoryDtoOut](request)
    }

    override def storeEloPoints(dtoIn: AddEloPointsDtoIn): Task[AddEloPointsDtoOut] = {
      val uri     = baseUri / "games" / "eloPoints" / "store"
      val request = Request[Task](POST, uri).withEntity(dtoIn)
      client.expect[AddEloPointsDtoOut](request)
    }

    override def listGameHistory(realm: String, season: String): Task[ListGameHistoryDtoOut] = {
      val uri = baseUri / "games" / "history" / "list"
      val uriWithParams = uri
        .withQueryParam(realm, s"$realm")
        .withQueryParam("season", s"$season")
      client.expect[ListGameHistoryDtoOut](uriWithParams)
    }

    override def listCalculatedEloPoints(users: List[String]): Task[ListEloPointsDtoOut] = {
      val uri = baseUri / "games" / "eloPoints" / "listCalculated"
      val uriWithParams = uri
        .withQueryParam("users", users.mkString(","))
      client.expect[ListEloPointsDtoOut](uriWithParams)
    }

    override def findPlayer(surname: String): Task[FindUserDtoOut] = {
      val uri           = baseUri / "users" / "find"
      val uriWithParams = uri.withQueryParam("surname", s"$surname")
      client.expect[FindUserDtoOut](uriWithParams)
    }
  }

  val layer: URLayer[Has[Client[Task]], HasArbiterClient] = (ArbiterClientService(_)).toLayer
}
