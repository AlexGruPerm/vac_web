package application

import comm.{AppConfig, ConfigHelper}
import excel.{ExportExcelCache, ImplExportExcelCache}
import server.WebServer
import zio.{ZIO, _}

object MainApp extends ZIOAppDefault{

  val logger: ZLogger[String, Unit] =
    new ZLogger[String, Unit] {
      override def apply(
                          trace: Trace,
                          fiberId: FiberId,
                          logLevel: LogLevel,
                          message: () => String,
                          cause: Cause[Any],
                          context: FiberRefs,
                          spans: List[LogSpan],
                          annotations: Map[String, String]
                        ): Unit =
        println(s"${java.time.Instant.now()} - ${logLevel.label} - ${message()}")
    }

  private val logic: ZIO[AppConfig,Throwable,Unit] =
    for {
      _ <- WebServer.startWebServer()
    } yield ()

  private val mainApp: ZIO[ZIOAppArgs, Throwable, Unit] = for {
    args <- ZIO.service[ZIOAppArgs]
    confLayer <- ConfigHelper.ConfigZLayer(args)
    _ <- ZIO.withLogger(
        logger.filterLogLevel(_ >= LogLevel.Debug)){
        logic.provide(confLayer)
      }.provide(Runtime.removeDefaultLoggers)
  } yield ()

  def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    mainApp.foldZIO(
      err => ZIO.logError(s"Exception - ${err.getMessage}").as(0),
      suc => ZIO.succeed(suc)
    )

}