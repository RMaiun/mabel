package dev.rmaiun.mabel.services

import dev.rmaiun.mabel.processors.AddPlayerProcessor.HasAddPlayerProcessor
import dev.rmaiun.mabel.processors.{ AddPlayerProcessor, CmdProcessor }
import zio._

object CmdProcessorStrategy {
  type HasCmdProcessorStrategy = Has[CmdProcessorStrategy.Service]

  trait Service {
    def selectProcessor(cmd: String): Task[CmdProcessor]
  }

  case class CmdProcessorStrategyService(addPlayerProcessor: AddPlayerProcessor) extends Service {
    override def selectProcessor(cmd: String): Task[CmdProcessor] =
      cmd match {
        case "addPlayer" => Task.succeed(addPlayerProcessor)
        case _           => Task.fail(new RuntimeException("Unable to process given request"))
      }
  }

  val layer: URLayer[HasAddPlayerProcessor, HasCmdProcessorStrategy] = (CmdProcessorStrategyService(_)).toLayer
}
