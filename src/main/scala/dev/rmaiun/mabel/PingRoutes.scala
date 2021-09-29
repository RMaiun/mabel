package dev.rmaiun.mabel

import dev.rmaiun.mabel.services.PingManager
import dev.rmaiun.mabel.services.PingManager.HasPingManager
import io.circe.{ Decoder, Encoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.dsl.Http4sDsl
import org.http4s.{ EntityDecoder, EntityEncoder, HttpRoutes }
import zio._
import zio.interop.catz._

object PingRoutes {
  def routes[R <: HasPingManager]: HttpRoutes[RIO[R, *]] = {
    type TodoTask[A] = RIO[R, A]

    val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
    import dsl._

    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[TodoTask, A] = jsonOf[TodoTask, A]
    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[TodoTask, A] = jsonEncoderOf[TodoTask, A]

    HttpRoutes.of[TodoTask] { case GET -> Root / "ping" =>
      for {
        s        <- PingManager.ping()
        response <- Ok(s)
      } yield response
    }
  }
}
