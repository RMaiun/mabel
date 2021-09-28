package dev.rmaiun.mabel.processors

import dev.rmaiun.mabel.dtos.{BotRequest, BotResponse}
import zio.Task

trait CmdProcessor {
  def process(input:BotRequest):Task[BotResponse]
}
