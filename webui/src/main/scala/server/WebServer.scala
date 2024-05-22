package server

import ch.{ClickhousePoolImpl, ClickhouseService, ClickhouseServiceImpl}
import comm.BadReq.ZioResponseMsgBadRequest
import comm.{AppConfig, ResponseMessage}
import excel.{ExportExcelCache, ImplExportExcelCache}
import rep1.Report1
import zio.{ZIO, ZLayer, durationInt}
import zio.http.netty.NettyConfig
import zio.http.template.{a, href, li, ul}
import zio.http.{Body, Handler, Headers, HttpApp, Method, Middleware, Path, Request, Response, Routes, Server, Status, handler, int, trailing}
import zio.json.EncoderOps

import java.io.File

object WebServer {

  /**
   * Add catchAll common part to effect.
   */
  private def catchCover[C](eff: ZIO[C, Throwable, Response]): ZIO[C, Nothing, Response] =
    eff.catchAll { e: Throwable =>
      ZIO.logError(e.getMessage) *> ZioResponseMsgBadRequest(e.getMessage)
    }

  private val routes: Routes[AppConfig with ClickhouseService with ExportExcelCache, Nothing] =
    Routes(
      Method.GET  / "report" / int("id") ->
        handler { (id: Int, req: Request) =>
          ZIO.logInfo(s"report_id = $id URL[${req.method.name}] - ${req.url.path.toString()}") *>
          catchCover(Report1.newEmptyMainPage())
        },
      Method.POST / "report" / int("id") / "show" ->
        handler { (id: Int, req: Request) =>
          ZIO.logInfo(s"report_id = $id URL[${req.method.name}] - ${req.url.path.toString()}") *>
        catchCover(Report1.getReport(req))
      },
      Method.POST / "report" / int("id") / "export" ->
        handler { (id: Int, req: Request) =>
          ZIO.logInfo(s"report_id = $id URL[${req.method.name}] - ${req.url.path.toString()}") *>
            catchCover(Report1.exportExcel(req))
        },
      Method.GET / "report"/ int("id") / "export/status/" / int("key") ->
        handler { (id: Int, key: Int,req: Request) =>
          ZIO.logInfo(s"report_id = 1 URL[${req.method.name}] - ${req.url.path.toString()}") *>
            catchCover(Report1.exportExcelPercent(id,key))
        }
    )  @@ Middleware. serveDirectory(Path. empty / "export", new File("E:\\tmp_excel")) //todo: use path from config "E:\\tmp_excel" Path.empty  / "static"

  val app: HttpApp[AppConfig with ClickhouseService with ExportExcelCache] = routes.toHttpApp

  def startWebServer(): ZIO[AppConfig, Throwable, Unit] = for {
    appConfig <- ZIO.service[AppConfig]
    clickhouseConfig <- ZIO.serviceWith[AppConfig](_.clickhouseConfig)
    webServerConfig = Server.Config.default.port(appConfig.port)
    nettyConfig     = NettyConfig.default.maxThreads(appConfig.nThreads)
    _ <-  (Server.install(app).flatMap { port =>
      ZIO.logInfo(s"Started server on port: $port")
    } *> ZIO.never)
      .provide(
          ImplExportExcelCache.layer,
          ZLayer.succeed(appConfig),
          ZLayer.succeed(clickhouseConfig),
          ClickhousePoolImpl.layer,
          ClickhouseServiceImpl.layer,
          ZLayer.succeed(webServerConfig),
          ZLayer.succeed(nettyConfig),
          Server.customized
      )
  } yield ()

}
