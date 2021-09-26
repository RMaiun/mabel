package dev.rmaiun.mabel

import zio.{ Clock, Console, Has, Task, URLayer }

object PingManager {
  type HasPingManager = Has[PingManager.Service]
  trait Service {
    def ping(): Task[String]
  }
  case class PingManagerLive(console: Console, clock: Clock) extends Service {
    override def ping(): Task[String] =
      for {
        c <- clock.currentDateTime
        _ <- console.printLine(c.toString)
      } yield c.toString
  }

  val layer: URLayer[Has[Console] with Has[Clock], HasPingManager] = (PingManagerLive(_, _)).toLayer

}
