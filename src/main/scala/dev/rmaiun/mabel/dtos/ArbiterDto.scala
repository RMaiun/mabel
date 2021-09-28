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

  case class GameHistoryDto(
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

  case class EloPointsDto(user: String, value: Int, created: ZonedDateTime)

  case class AddGameHistoryDtoIn(historyElement: GameHistoryDto, moderatorTid: Long)
  case class AddGameHistoryDtoOut(storedRound: GameHistoryDto)

  case class AddEloPointsDtoIn(points: EloPointsDto, moderatorTid: Long)
  case class AddEloPointsDtoOut(id: Long)

  case class ListGameHistoryDtoIn(realm: String, season: String)
  case class ListGameHistoryDtoOut(games: List[StoredGameHistoryDto])

  case class ListEloPointsDtoIn(users: Option[List[String]])
  case class ListEloPointsDtoOut(calculatedEloPoints: List[EloPointsDto], unratedPlayers: List[String] = Nil)

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

  case class FindAllUsersDtoIn(realm: String, activeStatus: Option[Boolean])
  case class FindAllUsersDtoOut(items: List[UserDto])

  case class FindUserDtoIn(surname: Option[String] = None, tid: Option[Long] = None)
  case class FindUserDtoOut(user: UserDto)

  case class AssignUserToRealmDtoIn(
    user: String,
    realm: String,
    role: Option[String],
    switchAsActive: Option[Boolean],
    moderatorTid: Long
  )
  case class AssignUserToRealmDtoOut(
    user: String,
    realm: String,
    role: String,
    assignedAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    switchedAsActive: Option[Boolean]
  )

  case class SwitchActiveRealmDtoIn(user: String, realm: String, moderatorTid: Long)
  case class SwitchActiveRealmDtoOut(activeRealm: String)

  case class ProcessActivationDtoIn(users: List[String], moderatorTid: Long, realm: String, activate: Boolean)
  case class ProcessActivationDtoOut(users: List[String], activationStatus: Boolean)

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

    implicit val GetRealmDtoInEncoder: Encoder[GetRealmDtoIn]   = deriveEncoder[GetRealmDtoIn]
    implicit val GetRealmDtoOutDecoder: Decoder[GetRealmDtoOut] = deriveDecoder[GetRealmDtoOut]

    implicit val RegisterUserDtoInEncoder: Encoder[RegisterUserDtoIn]   = deriveEncoder[RegisterUserDtoIn]
    implicit val RegisterUserDtoOutDecoder: Decoder[RegisterUserDtoOut] = deriveDecoder[RegisterUserDtoOut]
  }
}
