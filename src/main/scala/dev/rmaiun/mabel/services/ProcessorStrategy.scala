package dev.rmaiun.mabel.services

import dev.rmaiun.mabel.processors.AddPlayerProcessor.HasAddPlayerProcessor
import dev.rmaiun.mabel.processors.AddRoundProcessor.HasAddRoundProcessor
import dev.rmaiun.mabel.processors.{AddPlayerProcessor, AddRoundProcessor, Processor}
import zio._

object ProcessorStrategy {
  type HasProcessorStrategy = Has[ProcessorStrategy.Service]

  trait Service {
    def selectProcessor(cmd: String): Task[Processor]
  }

  case class CmdProcessorStrategyService(addPlayerProcessor: AddPlayerProcessor,
                                         addRoundProcessor: AddRoundProcessor) extends Service {
    override def selectProcessor(cmd: String): Task[Processor] =
      cmd match {
        case "addPlayer" => Task.succeed(addPlayerProcessor)
        case "addRound" => Task.succeed(addRoundProcessor)
        case _           => Task.fail(new RuntimeException("Unable to process given request"))
      }
  }

  val layer: URLayer[HasAddPlayerProcessor with HasAddRoundProcessor, HasProcessorStrategy] = (CmdProcessorStrategyService(_,_)).toLayer
}
