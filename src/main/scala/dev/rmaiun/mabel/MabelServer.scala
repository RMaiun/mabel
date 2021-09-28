package dev.rmaiun.mabel

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.Executors
import javax.net.ssl.{SSLContext, X509TrustManager}
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object MabelServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
val clientEC: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

    for {
      client <- BlazeClientBuilder[F](global).withMaxWaitQueueLimit(100).stream
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        MabelRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        MabelRoutes.jokeRoutes[F](jokeAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F](clientEC)
        .bindHttp(9091, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
