package dev.rmaiun.mabel.utils

import org.slf4j.Logger
import zio.Task

object Log {

  def info(msg: String)(implicit log: Logger): Task[Unit] =
    Task.effect(log.info(msg))

  def error(cause: Throwable)(implicit log: Logger): Task[Unit] =
    Task.effect(log.error("Error occurred", cause))
}
