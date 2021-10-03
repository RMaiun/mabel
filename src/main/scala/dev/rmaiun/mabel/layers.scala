package dev.rmaiun.mabel

import dev.rmaiun.mabel.processors.AddPlayerProcessor
import dev.rmaiun.mabel.processors.AddPlayerProcessor.HasAddPlayerProcessor
import dev.rmaiun.mabel.services.PingManager.HasPingManager
import dev.rmaiun.mabel.services.ProcessorStrategy.HasProcessorStrategy
import dev.rmaiun.mabel.services.{ ArbiterClient, Http4sClient, PingManager, ProcessorStrategy }
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
  type AppEnv    = HasProcessorStrategy with HasPingManager with Clock with Blocking

  object live {
    private lazy val http4sClient           = Http4sClient.Http4sClientLive.layer
    private lazy val arbiterClient          = http4sClient >>> ArbiterClient.layer
    private lazy val pingManager            = (Console.live ++ Clock.live) >>> PingManager.layer
    private lazy val addPlayerProcessor     = arbiterClient >>> AddPlayerProcessor.layer
    private lazy val processorStrategyLayer = addPlayerProcessor >>> ProcessorStrategy.layer
    lazy val appLayer: ZLayer[Any, Throwable, AppEnv] = {
      processorStrategyLayer ++
        pingManager ++
        Clock.live ++
        Blocking.live
    }
  }
}
