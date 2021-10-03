package dev.rmaiun.mabel.processors

import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.commands.AddPlayerCmd._
import dev.rmaiun.mabel.dtos.ArbiterDto.{ RegisterUserDtoIn, UserData }
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse, ProcessorResponse }
import dev.rmaiun.mabel.services.ArbiterClient.HasArbiterClient
import dev.rmaiun.mabel.services.{ ArbiterClient, IdGenerator }
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import zio._
case class AddPlayerProcessor(arbiterClient: ArbiterClient.Service) extends Processor {

  override def process(input: BotRequest): Task[ProcessorResponse] =
    for {
      dto             <- parseDto[AddPlayerCmd](input.data)
      userData         = UserData(dto.surname, Some(dto.tid))
      addPlayerResult <- arbiterClient.addPlayer(RegisterUserDtoIn(userData, dto.moderator))
    } yield {
      val result      = s"$PREFIX New player was registered with id ${addPlayerResult.user.id} $SUFFIX"
      val botResponse = BotResponse(input.chatId, IdGenerator.msgId, result)
      ProcessorResponse.ok(botResponse)
    }
}
object AddPlayerProcessor {
  type HasAddPlayerProcessor = Has[AddPlayerProcessor]
  val layer: URLayer[HasArbiterClient, HasAddPlayerProcessor] = (AddPlayerProcessor(_)).toLayer
}
