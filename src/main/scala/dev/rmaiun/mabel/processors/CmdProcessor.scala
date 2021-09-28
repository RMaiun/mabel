package dev.rmaiun.mabel.processors

import dev.rmaiun.mabel.dtos.{BotRequest, ProcessorResponse}
import zio.Task
import io.circe.{Decoder, Json}

trait CmdProcessor {
  def process(input: BotRequest): Task[ProcessorResponse]

  protected def parseDto[T](body: Option[Json])(implicit d: Decoder[T]): Task[T] =
    body match {
      case Some(value) => Task.fromEither(value.as[T])
      case None        => Task.fail(new RuntimeException("Bot request body is missed"))
    }
}
