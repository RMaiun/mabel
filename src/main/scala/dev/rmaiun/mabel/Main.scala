package dev.rmaiun.mabel

import cats.effect._
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.layers.AppEnv
import nl.vroste.zio.amqp.{Amqp, Channel}
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.slf4j.LoggerFactory
import zio.ZManaged.Finalizer
import zio.blocking.Blocking
import zio.console.{Console, putStrLn}
import zio.interop.catz._
import zio.{ExitCode => ZExitCode, _}

import java.net.URI
import scala.util.Random

object Main extends App {

  type AppTask[A] = RIO[layers.AppEnv, A]
  private val log = LoggerFactory.getLogger(Main.getClass)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ZExitCode] = {
    val prog =
      for {
        _        <- Task.effect(log.info("Starting ..."))
        _        <- publisher
        _        <- publisher
        channels <- channelsM.use(ch => Task.succeed(ch))
        p <- p2
        _ <- bla(p)
        _ <- bla(p)
        _        <- consumerEffect
        httpApp = Router[AppTask](
                    "/sys" -> PingRoutes.routes
                  ).orNotFound

        _ <- runHttp(httpApp, 9092)
      } yield ZExitCode.success

    prog
      .provideSomeLayer(layers.live.appLayer)
      .orDie
  }
  private def bla(c:Channel):Task[Unit] = {
    for{
     _ <- Task.effect(println(c))
    _ <- Task.effect(c.publish(
      "",
      BotRequest.BotRequestEncoder(BotRequest("addRound", "x1", 123, "bla")).toString().getBytes,
      "input_q"))
    }yield ()
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
  case class ChannelWrapper(c:Channel, int:Int)

  val channelsM: ZManaged[Blocking, Throwable, Channels] = for {
    connection <- Amqp.connect(URI.create("amqp://rabbitmq:rabbitmq@localhost:5672/arbiter?adminPort=15672"))
    channel1   <- Amqp.createChannel(connection)
    channel2   <- Amqp.createChannel(connection)
  } yield Channels(channel1, channel2)

  val processorEffect: ZManaged[Blocking, Throwable, ChannelWrapper] = channelsM.map(_.pCh).map(x => ChannelWrapper(x, Random.nextInt()))
  val publisher: ZIO[Blocking, Throwable, Unit] = processorEffect.use { ch =>
    println(ch.toString)
    ch.c.publish(
      "",
      BotRequest.BotRequestEncoder(BotRequest("addRound", "x1", 123, "bla")).toString().getBytes,
      "input_q"
    )
  }

  val p2 =  channelsM.map(_.pCh).useNow

  val consumerEffect: ZIO[Blocking with Console, Throwable, Unit] = channelsM.map(_.cCh).use { channel =>
    channel
      .consume(queue = "input_q", consumerTag = "test")
      .mapM { record =>
        val deliveryTag = record.getEnvelope.getDeliveryTag
        putStrLn(s"Received $deliveryTag: ${new String(record.getBody)}") *>
          channel.ack(deliveryTag)
      }
      .take(5)
      .runDrain
  }
}
