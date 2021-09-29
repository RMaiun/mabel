package dev.rmaiun.mabel

import cats.effect._
import dev.rmaiun.mabel.services.layers
import dev.rmaiun.mabel.services.layers.AppEnv
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import zio.interop.catz._
import zio.{ ExitCode => ZExitCode, _ }

object Main extends App {

  type AppTask[A] = RIO[layers.AppEnv, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ZExitCode] = {
    val prog =
      for {
        _ <- logging.log.info(s"Starting ...")
        httpApp = Router[AppTask](
                    "/sys" -> PingRoutes.routes
                  ).orNotFound

        _ <- runHttp(httpApp, 9092)
      } yield ZExitCode.success

    prog
      .provideSomeLayer(layers.live.appLayer)
      .orDie
  }

  def runHttp(
    httpApp: HttpApp[RIO[AppEnv, *]],
    port: Int
  ): ZIO[AppEnv, Throwable, Unit] = {
    type EnvTask[A] = RIO[AppEnv, A]

    ZIO.runtime[AppEnv].flatMap { implicit rts =>
      BlazeServerBuilder
        .apply[EnvTask](rts.platform.executor.asEC)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
        .compile[EnvTask, EnvTask, ExitCode]
        .drain
    }
  }
}
