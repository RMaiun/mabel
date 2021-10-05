package dev.rmaiun.mabel.services

import com.rabbitmq.client.Delivery
import dev.rmaiun.mabel.services.ProcessorStrategy.HasProcessorStrategy
import dev.rmaiun.mabel.utils.Log
import io.circe.Json
import io.circe.parser._
import org.slf4j.{Logger, LoggerFactory}
import zio._

object CommandHandler {
  private implicit val log: Logger = LoggerFactory.getLogger(CommandHandler.getClass)

  type HasCommandHandler = Has[CommandHandler.Service]
  val layer: URLayer[HasProcessorStrategy, HasCommandHandler] = (CommandHandlerService(_)).toLayer

  trait Service {
    def process(record: Delivery): Task[Unit]
  }
  case class CommandHandlerService(strategy: ProcessorStrategy.Service) extends Service {
    import dev.rmaiun.mabel.dtos.BotRequest._
    override def process(record: Delivery): Task[Unit] = {
      val start = System.currentTimeMillis
      val x = parse(new String(record.getBody)).getOrElse(Json.fromBoolean(true))
      val value = BotRequestDecoder.decodeJson(x)
      val flow = for {
//        json      <- Task.fromEither(parse(new String(record.getBody)))
//        _ <- Log.info(json.toString())
        input     <- Task.fromEither(value)
        _ <- Log.info(input.toString)
        processor <- strategy.selectProcessor(input.cmd)
        result    <- processor.process(input)
        _         <- Log.info(result.botResponse.result)
      } yield ()

      flow.fold(
        err => Log.error(err),
        x => Log.info("ok"))
    }
  }

  def process(record: Delivery): RIO[HasCommandHandler, Unit] =
    ZIO.access(_.get.process(record))
}
