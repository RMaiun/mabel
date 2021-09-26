package dev.rmaiun.mabel

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    MabelServer.stream[IO].compile.drain.as(ExitCode.Success)
}
