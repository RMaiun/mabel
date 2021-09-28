package dev.rmaiun.mabel

import dev.rmaiun.mabel.processors.CmdProcessor
import zio.{IO, Task}

object CmdProcessorStrategy {

  def selectProcessor(cmd:String):Task[CmdProcessor] = {
    cmd match {
      case "addPlayer" => Task.succeed()
      case _ => Task.fail(new RuntimeException("Unable to process given request"))
    }
  }
}
