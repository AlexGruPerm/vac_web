package server

import ch.{ClickhousePoolImpl, ClickhouseService, ClickhouseServiceImpl}
import comm.BadReq.ZioResponseMsgBadRequest
import comm.{AppConfig, ReqReport1, ResponseMessage}
import excel.{ExportExcelCache, ImplExportExcelCache}
import rep1.Report1
import rep2.Report2
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

  //http://localhost:8081/report/1?period=24290
  //Use this way to send arrays:
  //http://localhost:8081/report/1?period=24290&omsu=441&omsu=1870&org=6085&org=100500
  //http://localhost:8081/report/1?period=24289&omsu=441&omsu=1870&org=6085&org=100500&errls=1
  //http://localhost:8081/report/1?period=24289&omsu=441&omsu=1870&errls=1&existpd=1
  //http://localhost:8081/report/1?period=24289&omsu=441&omsu=1870&errls=1&existpd=-1
  private val routes: String => Routes[AppConfig with ClickhouseService with ExportExcelCache, Nothing] = excelPath => {
      Routes(
        Method.GET / "report" / int("id")  ->
          handler { (id: Int, req: Request) =>
            ZIO.logInfo(s"report_id = $id URL[${req.method.name}] - ${req.url.path.toString()}") *>
              (if (req.url.queryParams.isEmpty){
                (id match {
                  case 1 => catchCover(Report1.newEmptyMainPage())
                  case _ => ZioResponseMsgBadRequest("Report id not found.")
                })
          } else{
                ZIO.logInfo(s" query params ${req.url} ") *>
                ZIO.logInfo(s" PARSED: period = ${req.url.queryParams.getAll("period").head.toInt}") *>
                ZIO.logInfo(s" PARSED: errls  = ${req.url.queryParams.getAll("errls").head.toInt}") *>
                ZIO.logInfo(s" PARSED: existpd  = ${req.url.queryParams.getAll("existpd").head.toInt}") *>
                ZIO.logInfo(s" PARSED: omsu(s) = ${req.url.queryParams.getAll("omsu").toList.map(_.toInt)}") *>
                ZIO.logInfo(s" PARSED: org(s)  = ${req.url.queryParams.getAll("org").toList.map(_.toInt)}") *>
                ZIO.logInfo(s" PARSED: page_num  = ${req.url.queryParams.getAll("page_num").toList.map(_.toInt)}") *>
                  ZIO.logInfo(s" PARSED: page_cnt  = ${req.url.queryParams.getAll("page_cnt").toList.map(_.toInt)}") *>
                ZIO.logInfo(s" PARSED: errlstxt  = ${req.url.queryParams.getAll("errlstxt").toList}") *>
                  ZIO.logInfo(s" PARSED: errlstxt ? isEmpty = ${req.url.queryParams.getAll("errlstxt").toList.isEmpty}") *>
                catchCover(
                  Report1.newEmptyMainPageRun(
                    ReqReport1(
                      period = req.url.queryParams.getAll("period").head.toInt,
                      omsu = req.url.queryParams.getAll("omsu").toList.map(_.toInt),
                      org = req.url.queryParams.getAll("org").toList.map(_.toInt),
                      errls   = req.url.queryParams.getAll("errls").head.toInt,
                      /*if (req.url.queryParams.getAll("errls").head.toInt == -1 && req.url.queryParams.getAll("errlstxt").toList.isEmpty)
                      0 else req.url.queryParams.getAll("errls").head.toInt*/
                      existpd = req.url.queryParams.getAll("existpd").head.toInt,
                      page_num = req.url.queryParams.getAll("page_num").head.toInt,
                      page_cnt = req.url.queryParams.getAll("page_cnt").head.toInt,
                      errlstxt = req.url.queryParams.getAll("errlstxt").toList
                    )
                  )
                )
             })
          },
/*        Method.GET / "report" / int("id") ->
          handler { (id: Int, req: Request) =>
            ZIO.logInfo(s"report_id = $id URL[${req.method.name}] - ${req.url.path.toString()}") *>
              catchCover(Report1.newEmptyMainPage())
          },*/
        Method.POST / "report" / int("id") / "show" ->
          handler { (id: Int, req: Request) =>
            ZIO.logInfo(s"report_id = $id URL[${req.method.name}] - ${req.url.path.toString()}") *>
              (id match {
                  case 1 => catchCover(Report1.getReport(req))
                  case 2 => catchCover(Report2.getReport(req))
                  case _ => ZioResponseMsgBadRequest("Report id not found.")
                })
          },
        Method.POST / "report" / int("id") / "export" ->
          handler { (id: Int, req: Request) =>
            ZIO.logInfo(s"report_id = $id URL[${req.method.name}] - ${req.url.path.toString()}") *>
              catchCover(Report1.exportExcel(req))
          },
        Method.GET / "report" / int("id") / "export/status/" / int("key") ->
          handler { (id: Int, key: Int, req: Request) =>
            ZIO.logInfo(s"report_id = 1 URL[${req.method.name}] - ${req.url.path.toString()}") *>
              catchCover(Report1.exportExcelPercent(id, key))
          }
      ) @@ Middleware.serveDirectory(Path.empty / "export", new File(excelPath))
    }

  val app: String => HttpApp[AppConfig with ClickhouseService with ExportExcelCache] =
    excelPath => routes(excelPath).toHttpApp

  def startWebServer(): ZIO[AppConfig, Throwable, Unit] = for {
    appConfig <- ZIO.service[AppConfig]
    clickhouseConfig <- ZIO.serviceWith[AppConfig](_.clickhouseConfig)
    webServerConfig = Server.Config.default.port(appConfig.port)
    nettyConfig     = NettyConfig.default.maxThreads(appConfig.nThreads)
    _ <-  (Server.install(app(appConfig.excelPath)).flatMap { port =>
      ZIO.logInfo(s"Started server on port: $port excel path: ${appConfig.excelPath}")
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
