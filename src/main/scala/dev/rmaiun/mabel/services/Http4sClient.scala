package dev.rmaiun.mabel.services

import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import zio.{ Has, Task, ZEnv, ZLayer, Runtime => ZRuntime }
import zio.interop.catz._

object Http4sClient {
  object Http4sClientLive {
    val layer: ZLayer[Any, Throwable, Has[Client[Task]]] = {
      implicit val runtime: ZRuntime[ZEnv] = ZRuntime.default
      val res                              = BlazeClientBuilder[Task](runtime.platform.executor.asEC).resource.toManagedZIO
      ZLayer.fromManaged(res)
    }
  }
}
