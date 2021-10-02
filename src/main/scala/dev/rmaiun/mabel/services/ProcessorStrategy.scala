package dev.rmaiun.mabel.services

import dev.rmaiun.mabel.processors.AddPlayerProcessor.HasAddPlayerProcessor
import dev.rmaiun.mabel.processors.{ AddPlayerProcessor, Processor }
import zio._

object ProcessorStrategy {
  type HasProcessorStrategy = Has[ProcessorStrategy.Service]

  trait Service {
    def selectProcessor(cmd: String): Task[Processor]
  }

  case class CmdProcessorStrategyService(addPlayerProcessor: AddPlayerProcessor) extends Service {
    override def selectProcessor(cmd: String): Task[Processor] =
      cmd match {
        case "addPlayer" => Task.succeed(addPlayerProcessor)
        case _           => Task.fail(new RuntimeException("Unable to process given request"))
      }
  }

  val layer: URLayer[HasAddPlayerProcessor, HasProcessorStrategy] = (CmdProcessorStrategyService(_)).toLayer
}
