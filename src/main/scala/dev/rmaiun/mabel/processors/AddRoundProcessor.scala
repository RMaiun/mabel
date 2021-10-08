package dev.rmaiun.mabel.processors

import dev.rmaiun.mabel.commands.AddRoundCmd
import dev.rmaiun.mabel.commands.AddRoundCmd._
import dev.rmaiun.mabel.dtos.ArbiterDto._
import dev.rmaiun.mabel.dtos.EloRatingDto.{ CalculatedPoints, EloPlayers, UserCalculatedPoints }
import dev.rmaiun.mabel.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.mabel.helpers.{ DateHelper, SeasonHelper }
import dev.rmaiun.mabel.services.ArbiterClient.HasArbiterClient
import dev.rmaiun.mabel.services.EloPointsCalculator.HasEloPointsCalculator
import dev.rmaiun.mabel.services.{ ArbiterClient, EloPointsCalculator, IdGenerator }
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.{ Constants, Log }
import org.slf4j.{ Logger, LoggerFactory }
import zio._

case class AddRoundProcessor(arbiterClient: ArbiterClient.Service, eloPointsCalculator: EloPointsCalculator.Service)
    extends Processor {
  private implicit val log: Logger = LoggerFactory.getLogger(AddRoundProcessor.getClass)

  override def process(input: BotRequest): Task[ProcessorResponse] =
    for {
      dto           <- parseDto[AddRoundCmd](input.data)
      w1            <- loadPlayer(dto.w1)
      w2            <- loadPlayer(dto.w2)
      l1            <- loadPlayer(dto.l1)
      l2            <- loadPlayer(dto.l2)
      userPoints    <- calculateEloPoints(w1, w2, l1, l2)
      pointsIdList  <- storeEloPoints(userPoints, dto.moderator)
      _             <- Log.info(s"Elo points were successfully stored with id: ${pointsIdList.mkString("[", ",", "]")}")
      storedHistory <- storeHistory(dto)
    } yield {
      val msg = formatMessage(storedHistory.storedRound.id, storedHistory.storedRound.realm)
      ProcessorResponse.ok(input.chatId, IdGenerator.msgId, msg)
    }

  private def formatMessage(id: Long, realm: String): String =
    s"$PREFIX New game was stored with id $id for realm $realm $SUFFIX"

  private def storeHistory(dto: AddRoundCmd): Task[AddGameHistoryDtoOut] = {
    val ghDto = GameHistoryDtoIn(
      Constants.defaultRealm,
      SeasonHelper.currentSeason,
      dto.w1.toLowerCase,
      dto.w2.toLowerCase,
      dto.l1.toLowerCase,
      dto.l2.toLowerCase,
      dto.shutout
    )
    arbiterClient.storeGameHistory(AddGameHistoryDtoIn(ghDto, dto.moderator))
  }
  private def loadPlayer(surname: String): Task[FindUserDtoOut] =
    arbiterClient.findPlayer(surname.toLowerCase)

  private def calculateEloPoints(
    w1: FindUserDtoOut,
    w2: FindUserDtoOut,
    l1: FindUserDtoOut,
    l2: FindUserDtoOut
  ): Task[UserCalculatedPoints] =
    eloPointsCalculator.calculate(EloPlayers(w1.user.surname, w2.user.surname, l1.user.surname, l2.user.surname))

  private def storeEloPoints(data: UserCalculatedPoints, moderatorTid: Long): Task[List[Long]] = {
    val w1Dto = formDto(data.w1, moderatorTid)
    val w2Dto = formDto(data.w2, moderatorTid)
    val l1Dto = formDto(data.l1, moderatorTid)
    val l2Dto = formDto(data.l2, moderatorTid)
    for {
      out1 <- arbiterClient.storeEloPoints(w1Dto)
      out2 <- arbiterClient.storeEloPoints(w2Dto)
      out3 <- arbiterClient.storeEloPoints(l1Dto)
      out4 <- arbiterClient.storeEloPoints(l2Dto)
    } yield List(out1.id, out2.id, out3.id, out4.id)
  }

  private def formDto(dto: CalculatedPoints, moderatorTid: Long): AddEloPointsDtoIn =
    AddEloPointsDtoIn(EloPointsDto(dto.player, dto.points, DateHelper.now), moderatorTid)
}

object AddRoundProcessor {
  type HasAddRoundProcessor = Has[AddRoundProcessor]
  val layer: URLayer[HasArbiterClient with HasEloPointsCalculator, HasAddRoundProcessor] =
    (AddRoundProcessor(_, _)).toLayer
}
