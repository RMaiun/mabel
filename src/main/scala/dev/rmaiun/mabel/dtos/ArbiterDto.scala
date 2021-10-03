package dev.rmaiun.mabel.dtos

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

import java.time.{ ZoneOffset, ZonedDateTime }

object ArbiterDto {
  case class RealmDto(id: Long, name: String, selectedAlgorithm: Option[String], teamSize: Int)

  case class GetRealmDtoIn(realm: String)
  case class GetRealmDtoOut(realm: RealmDto)

  case class RegisterRealmDtoIn(realmName: String, algorithm: String, teamSize: Int)
  case class RegisterRealmDtoOut(realm: RealmDto)

  case class UpdateRealmAlgorithmDtoIn(id: Long, algorithm: String)
  case class UpdateRealmAlgorithmDtoOut(realm: RealmDto)

  case class GameHistoryDtoIn(
    realm: String,
    season: String,
    w1: String,
    w2: String,
    l1: String,
    l2: String,
    shutout: Boolean = false
  )

  case class GameHistoryDto(
    id: Long,
    realm: String,
    season: String,
    w1: String,
    w2: String,
    l1: String,
    l2: String,
    shutout: Boolean = false
  )
  case class StoredGameHistoryDto(
    realm: String,
    season: String,
    w1: String,
    w2: String,
    l1: String,
    l2: String,
    shutout: Boolean = false,
    createdAt: ZonedDateTime
  )
  case class RealmShortInfo(name: String, role: String, botUsage: Boolean)

  case class EloPointsDto(user: String, value: Int, created: ZonedDateTime)
  case class CalculatedEloPointsDto(user: String, value: Int, gamesPlayed: Int)

  case class AddGameHistoryDtoIn(historyElement: GameHistoryDtoIn, moderatorTid: Long)
  case class AddGameHistoryDtoOut(storedRound: GameHistoryDto)

  case class AddEloPointsDtoIn(points: EloPointsDto, moderatorTid: Long)
  case class AddEloPointsDtoOut(id: Long)

  case class ListGameHistoryDtoOut(games: List[StoredGameHistoryDto])

  case class ListEloPointsDtoOut(calculatedEloPoints: List[CalculatedEloPointsDto], unratedPlayers: List[String] = Nil)

  case class UserDto(
    id: Long,
    surname: String,
    nickname: Option[String] = None,
    tid: Option[Long] = None,
    active: Boolean = true,
    createdAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
  )
  case class UserData(surname: String, tid: Option[Long] = None)

  case class RegisterUserDtoIn(user: UserData, moderatorTid: Long)
  case class RegisterUserDtoOut(user: UserDto)

  case class FindUserDtoOut(user: UserDto, realms: List[RealmShortInfo])

  case class LinkTidDtoIn(tid: Long, nameToLink: String, moderatorTid: Long, realm: String)
  case class LinkTidDtoOut(
    subscribedSurname: String,
    subscribedTid: Long,
    createdDateTime: ZonedDateTime
  )

  case class FindAvailableRealmsDtoIn(surname: String)
  case class FindAvailableRealmsDtoOut(availableRealms: List[RealmDto])

  object codec {
    implicit val RealmDtoEncoder: Encoder[RealmDto] = deriveEncoder[RealmDto]
    implicit val RealmDtoDecoder: Decoder[RealmDto] = deriveDecoder[RealmDto]

    implicit val UserDataEncoder: Encoder[UserData] = deriveEncoder[UserData]
    implicit val UserDataDecoder: Decoder[UserData] = deriveDecoder[UserData]

    implicit val UserDtoEncoder: Encoder[UserDto] = deriveEncoder[UserDto]
    implicit val UserDtoDecoder: Decoder[UserDto] = deriveDecoder[UserDto]

    implicit val EloPointsDtoEncoder: Encoder[EloPointsDto] = deriveEncoder[EloPointsDto]
    implicit val EloPointsDtoDecoder: Decoder[EloPointsDto] = deriveDecoder[EloPointsDto]

    implicit val RealmShortInfoEncoder: Encoder[RealmShortInfo] = deriveEncoder[RealmShortInfo]
    implicit val RealmShortInfoDecoder: Decoder[RealmShortInfo] = deriveDecoder[RealmShortInfo]

    implicit val StoredGameHistoryDtoEncoder: Encoder[StoredGameHistoryDto] = deriveEncoder[StoredGameHistoryDto]
    implicit val StoredGameHistoryDtoDecoder: Decoder[StoredGameHistoryDto] = deriveDecoder[StoredGameHistoryDto]

    implicit val CalculatedEloPointsDtoEncoder: Encoder[CalculatedEloPointsDto] = deriveEncoder[CalculatedEloPointsDto]
    implicit val CalculatedEloPointsDtoDecoder: Decoder[CalculatedEloPointsDto] = deriveDecoder[CalculatedEloPointsDto]

    implicit val GameHistoryDtoInEncoder: Encoder[GameHistoryDtoIn] = deriveEncoder[GameHistoryDtoIn]
    implicit val GameHistoryDtoDecoder: Decoder[GameHistoryDto] = deriveDecoder[GameHistoryDto]

    implicit val GetRealmDtoInEncoder: Encoder[GetRealmDtoIn]   = deriveEncoder[GetRealmDtoIn]
    implicit val GetRealmDtoOutDecoder: Decoder[GetRealmDtoOut] = deriveDecoder[GetRealmDtoOut]

    implicit val RegisterUserDtoInEncoder: Encoder[RegisterUserDtoIn]   = deriveEncoder[RegisterUserDtoIn]
    implicit val RegisterUserDtoOutDecoder: Decoder[RegisterUserDtoOut] = deriveDecoder[RegisterUserDtoOut]

    implicit val AddGameHistoryDtoInEncoder: Encoder[AddGameHistoryDtoIn]   = deriveEncoder[AddGameHistoryDtoIn]
    implicit val AddGameHistoryDtoOutDecoder: Decoder[AddGameHistoryDtoOut] = deriveDecoder[AddGameHistoryDtoOut]

    implicit val AddEloPointsDtoInEncoder: Encoder[AddEloPointsDtoIn]   = deriveEncoder[AddEloPointsDtoIn]
    implicit val AddEloPointsDtoOutDecoder: Decoder[AddEloPointsDtoOut] = deriveDecoder[AddEloPointsDtoOut]

    implicit val ListEloPointsDtoOutDecoder: Decoder[ListEloPointsDtoOut] = deriveDecoder[ListEloPointsDtoOut]

    implicit val ListGameHistoryDtoOutDecoder: Decoder[ListGameHistoryDtoOut] = deriveDecoder[ListGameHistoryDtoOut]

    implicit val FindUserDtoOutDecoder: Decoder[FindUserDtoOut] = deriveDecoder[FindUserDtoOut]
  }
}
