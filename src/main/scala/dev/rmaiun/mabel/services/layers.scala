package dev.rmaiun.mabel.services

import dev.rmaiun.mabel.processors.AddPlayerProcessor.HasAddPlayerProcessor
import dev.rmaiun.mabel.services.CmdProcessorStrategy.HasCmdProcessorStrategy
import dev.rmaiun.mabel.services.PingManager.HasPingManager
import org.http4s.client.Client
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.{ Has, Task, ZLayer }

object layers {
  type HasClient = Has[Client[Task]]
  type Layer0Env = Logging with Console with Clock
  type Layer1Env = Layer0Env with HasPingManager
  type Layer2Env = Layer1Env with HasAddPlayerProcessor
  type Layer3Env = Layer2Env with HasCmdProcessorStrategy
  type AppEnv    = HasPingManager with Logging with Clock with Blocking

  object live {
    private val logger                         = Slf4jLogger.make((_, msg) => msg)
    private val consoleClock                   = Console.live ++ Clock.live
    private val layer1                         = (Console.live ++ Clock.live) >>> PingManager.layer
    val appLayer: ZLayer[Any, Nothing, AppEnv] = layer1 ++ logger ++ Clock.live ++ Blocking.live
  }
}
