package dev.rmaiun.mabel

import dev.rmaiun.mabel.processors.AddPlayerProcessor.HasAddPlayerProcessor
import dev.rmaiun.mabel.processors.{ AddPlayerProcessor, AddRoundProcessor }
import dev.rmaiun.mabel.services.CommandHandler.HasCommandHandler
import dev.rmaiun.mabel.services.PingManager.HasPingManager
import dev.rmaiun.mabel.services.ProcessorStrategy.HasProcessorStrategy
import dev.rmaiun.mabel.services._
import org.http4s.client.Client
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.{ Has, Task, ZLayer }

object layers {
  type HasClient = Has[Client[Task]]
  type Layer0Env = Console with Clock
  type Layer1Env = Layer0Env with HasPingManager
  type Layer2Env = Layer1Env with HasAddPlayerProcessor
  type Layer3Env = Layer2Env with HasProcessorStrategy
  type AppEnv    = HasCommandHandler with HasPingManager with Clock with Blocking with Console

  object live {
    private lazy val http4sClient        = Http4sClient.Http4sClientLive.layer
    private lazy val arbiterClient       = http4sClient >>> ArbiterClient.layer
    private lazy val eloPointsCalculator = arbiterClient >>> EloPointsCalculator.layer
    private lazy val pingManager         = (Console.live ++ Clock.live) >>> PingManager.layer
    private lazy val addPlayerProcessor  = arbiterClient >>> AddPlayerProcessor.layer
    private lazy val addRoundProcessor   = (arbiterClient ++ eloPointsCalculator) >>> AddRoundProcessor.layer
    private lazy val processorStrategy   = (addPlayerProcessor ++ addRoundProcessor) >>> ProcessorStrategy.layer
    private lazy val commandHandler      = processorStrategy >>> CommandHandler.layer
    lazy val appLayer: ZLayer[Any, Throwable, AppEnv] = {
      commandHandler ++
        pingManager ++
        Console.live ++
        Clock.live ++
        Blocking.live

    }
  }
}
