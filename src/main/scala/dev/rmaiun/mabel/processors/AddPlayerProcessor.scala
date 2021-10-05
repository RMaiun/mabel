package dev.rmaiun.mabel.processors

import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.commands.AddPlayerCmd._
import dev.rmaiun.mabel.dtos.ArbiterDto._
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse, ProcessorResponse }
import dev.rmaiun.mabel.services.ArbiterClient.HasArbiterClient
import dev.rmaiun.mabel.services.{ ArbiterClient, IdGenerator }
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.{ Constants, Log }
import org.slf4j.{ Logger, LoggerFactory }
import zio._
case class AddPlayerProcessor(arbiterClient: ArbiterClient.Service) extends Processor {
  private implicit val log: Logger = LoggerFactory.getLogger(AddRoundProcessor.getClass)

  override def process(input: BotRequest): Task[ProcessorResponse] =
    for {
      dto             <- parseDto[AddPlayerCmd](input.data)
      addPlayerResult <- registerPlayer(dto)
      _               <- assignUserToRealm(dto)
      _               <- Log.info(s"Registered new player ${addPlayerResult.user.surname} with id ${addPlayerResult.user.id}")
    } yield {
      val result      = s"$PREFIX New player was registered with id ${addPlayerResult.user.id} $SUFFIX"
      val botResponse = BotResponse(input.chatId, IdGenerator.msgId, result)
      ProcessorResponse.ok(botResponse)
    }

  private def registerPlayer(dto: AddPlayerCmd): Task[RegisterUserDtoOut] = {
    val userData = UserData(dto.surname, Some(dto.tid))
    arbiterClient.addPlayer(RegisterUserDtoIn(userData, dto.moderator))
  }

  private def assignUserToRealm(dto: AddPlayerCmd): Task[AssignUserToRealmDtoOut] = {
    val role = if (dto.admin) Some("RealmAdmin") else None
    val requestDto =
      AssignUserToRealmDtoIn(dto.surname.toLowerCase, Constants.defaultRealm, role, Some(true), dto.moderator)
    arbiterClient.assignUserToRealm(requestDto)
  }
}
object AddPlayerProcessor {
  type HasAddPlayerProcessor = Has[AddPlayerProcessor]
  val layer: URLayer[HasArbiterClient, HasAddPlayerProcessor] = (AddPlayerProcessor(_)).toLayer
}
