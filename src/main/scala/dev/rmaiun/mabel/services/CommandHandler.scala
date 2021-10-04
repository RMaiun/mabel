package dev.rmaiun.mabel.services

import com.rabbitmq.client.Delivery
import dev.rmaiun.mabel.services.ProcessorStrategy.HasProcessorStrategy
import dev.rmaiun.mabel.utils.Log
import io.circe.parser._
import org.slf4j.{ Logger, LoggerFactory }
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
      for {
        json      <- Task.fromEither(parse(new String(record.getBody)))
        input     <- Task.fromEither(BotRequestDecoder.decodeJson(json))
        processor <- strategy.selectProcessor(input.cmd)
        result    <- processor.process(input)
        _         <- Log.info(result.botResponse.result)
      } yield ()
    }
  }
}
