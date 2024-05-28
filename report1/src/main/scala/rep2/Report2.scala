package rep2

import ch.ClickhouseService
import comm.{AppConfig, Report1JsonTemplate, Report2JsonTemplate, ReqReport2, SummaryReport2}
import comm.BadReq.ZioResponseMsgBadRequest
import rep1.EncDecReqReport
import rep_common.ReqEntity.requestToEntity
import zio.{Clock, ZIO}
import zio.http.{Request, Response}
import zio.json.EncoderOps

import java.util.concurrent.TimeUnit

object Report2 {
  private val reportId: Int = 2

  import comm.EncDecReport2JsonTemplate._
  def getReport2DataJson(reqParams: ReqReport2): ZIO[AppConfig with ClickhouseService, Throwable, Response] =
    for {
      chService <- ZIO.service[ClickhouseService]
      header <- chService.getReport2Header(reportId)
      cacheKey = reqParams.hashCode()
      start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      /*
      sCheckCacheEx  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      isCacheExists <- chService.isCacheExistsCh(cacheKey)
      fCheckCacheEx  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- ZIO.logInfo(s"getReport1DataJson isCacheExists = $isCacheExists, ${fCheckCacheEx - sCheckCacheEx} ms")
      */

      data <- chService.getReportData2(reportId,reqParams)
      finish  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- ZIO.logInfo(s"getReportData2 ${finish-start} ms.")

      response <- ZIO.succeed(Response.json(Report2JsonTemplate(header,data).toJson))
    } yield response

  import EncDecReqReport._
  def getReport(req: Request): ZIO[AppConfig with ClickhouseService, Throwable, Response] = for {
    debugReq <- req.body.asString
    _ <- ZIO.logInfo(s"req $debugReq")
    reqEntity <- requestToEntity[ReqReport2](req)
    response <- reqEntity match {
      case Left(errorString) => ZioResponseMsgBadRequest(errorString)
      case Right(report2Request) => getReport2DataJson(report2Request)
      //getReport1DataHtml(report1Request)
    }
  } yield response

}
