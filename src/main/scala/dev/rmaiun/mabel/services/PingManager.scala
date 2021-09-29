package dev.rmaiun.mabel.services

import zio.clock.Clock
import zio.console.Console
import zio.{ Has, Task, URLayer, ZIO }

object PingManager {
  type HasPingManager = Has[PingManager.Service]
  trait Service {
    def ping(): Task[String]
  }
  case class PingManagerLive(console: Console.Service, clock: Clock.Service) extends Service {
    override def ping(): Task[String] =
      for {
        c <- clock.currentDateTime
        _ <- console.putStr(c.toString)
      } yield c.toString
  }

  val layer: URLayer[Console with Clock, HasPingManager] =
    ((console, clock) => PingManagerLive(console, clock)).toLayer

  def ping(): ZIO[HasPingManager, Throwable, String] = ZIO.accessM(_.get.ping())
}
