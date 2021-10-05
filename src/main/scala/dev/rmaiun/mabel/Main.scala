package dev.rmaiun.mabel

import cats.effect._
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.layers.AppEnv
import dev.rmaiun.mabel.services.CommandHandler
import dev.rmaiun.mabel.services.CommandHandler.HasCommandHandler
import dev.rmaiun.mabel.utils.Log
import nl.vroste.zio.amqp.{ Amqp, Channel }
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.slf4j.{ Logger, LoggerFactory }
import zio.blocking.Blocking
import zio.console.{ putStrLn, Console }
import zio.interop.catz._
import zio.{ ExitCode => ZExitCode, _ }

import java.net.URI

object Main extends App {

  type AppTask[A] = RIO[layers.AppEnv, A]
  private implicit val log: Logger = LoggerFactory.getLogger(Main.getClass)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ZExitCode] = {
    val prog =
      for {
        _ <- Task.effect(log.info("Starting server..."))
        _        <- consumerEffect.fork
        httpApp = Router[AppTask](
                    "/sys" -> PingRoutes.routes
                  ).orNotFound
        _ <- Log.info("Router initialized")
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
  case class Channels(pCh: Channel, cCh: Channel)
  case class ChannelWrapper(c: Channel, int: Int)

  val channelsM: ZManaged[Blocking, Throwable, Channels] = for {
    connection <- Amqp.connect(URI.create("amqp://rabbitmq:rabbitmq@localhost:5672/arbiter?adminPort=15672"))
    channel1   <- Amqp.createChannel(connection)
    channel2   <- Amqp.createChannel(connection)
  } yield Channels(channel1, channel2)

  val consumerEffect: ZIO[Blocking with Console with HasCommandHandler, Throwable, Unit] =
    channelsM.map(_.cCh).use { channel =>
      channel
        .consume(queue = "input_q", consumerTag = "test")
        .mapM { record =>
          val deliveryTag = record.getEnvelope.getDeliveryTag
          Log.info(s"Received $deliveryTag: ${new String(record.getBody)}") *>
            CommandHandler.process(record) *>
            channel.ack(deliveryTag)
        }
//      .take(5)
        .runDrain
    }
}
