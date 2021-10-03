package dev.rmaiun.mabel.services

import com.rabbitmq.client.Delivery
import nl.vroste.zio.amqp.{Amqp, Channel}
import zio.{Has, Task, ZIO, ZManaged}
import zio.blocking.Blocking
import zio.console.{Console, putStrLn}
import io.circe._, io.circe.parser._

import java.net.URI

object CommandHandler {

  val channelM: ZManaged[Blocking, Throwable, Channel] = for {
    connection <- Amqp.connect(URI.create("amqp://my_amqp_server_uri"))
    channel <- Amqp.createChannel(connection)
  } yield channel

  val effect: ZIO[Blocking with Console, Throwable, Unit] = channelM.use { channel =>
    channel
      .consume(queue = "queueName", consumerTag = "test")
      .mapM { record =>
        val deliveryTag = record.getEnvelope.getDeliveryTag
        putStrLn(s"Received ${deliveryTag}: ${new String(record.getBody)}") *>
          channel.ack(deliveryTag)
      }
      .take(5)
      .runDrain
  }
type HasCommandHandler = Has[CommandHandler.Service]
  trait Service{
    def process(record:Delivery):Task[Unit]
  }
  case class CommandHandlerService() extends Service{
    override def process(record: Delivery): Task[Unit] = {
      val start = System.currentTimeMillis
      for{
        json <- Task.fromEither(parse(new String(record.getBody)))

      }yield
    }
  }
}
