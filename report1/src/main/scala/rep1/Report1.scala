package rep1

import ch.ClickhouseService
import comm.BadReq.ZioResponseMsgBadRequest
import comm.TimestampConverter.TsToString
import comm.{AppConfig, HeadInfo, MultiSelect, Report1JsonTemplate, Report1Row, ReportHeaderRow, ReqReport1, SimpleDictData, SimpleDictMeta, SimpleDictRow, SingleSelect, SummaryReport1}
import excel.EncDecReport1JsonTemplate.encoderExportSuccessStarted
import excel.{ExportExcelCache, ExportProgress, ExportSuccessStarted, SingleExcelExportEntity, Success, Wait}
import org.apache.poi.xssf.usermodel.{XSSFSheet, XSSFWorkbook}
import rep1.html.PageHtml
import zio.http.template.div
import zio.http.{Body, Handler, Headers, Path, Request, Response, Status}
import zio.json.{DecoderOps, EncoderOps, JsonDecoder}
import zio.{Clock, ZIO}

import java.io.{File, FileOutputStream}
import java.sql.{ResultSet, SQLException}
import java.util.concurrent.TimeUnit
import scala.annotation.nowarn


object Report1 {

  private val reportId: Int = 1
  private val updateCacheEntityAfterRowCount: Int = 1000

  private val reportDictionaries: List[SimpleDictMeta] = List(
    SimpleDictMeta("errlstxt","Поиск по ошибкам ЛС",MultiSelect,Some(-1),
      "SELECT row_number() over()::Int8 as id,name FROM data.popup_errls"),
    SimpleDictMeta("orgs",  "Организации"  ,MultiSelect,None,      "select * from data.filter_org"),
    SimpleDictMeta("omsu",  "ОМСУ"         ,MultiSelect,Some(1870),"select * from data.filter_omsu"),
    SimpleDictMeta("extpd", "Наличие ПД"   ,SingleSelect,Some(-1), "select * from data.filter_exist_pd"),
    SimpleDictMeta("errls", "Ошибки в ЛС"  ,SingleSelect,Some(-1), "select * from data.filter_errls"),
    SimpleDictMeta("period","Период"       ,SingleSelect,None,
      "select p.id,concat(p.name,'.',(-1*p.parent_id)::String) as name from data.filter_period p where selectable=true"),
    SimpleDictMeta("errpd", "Ошибка ПД"   ,SingleSelect,Some(-1),  "select * from data.filter_errpd")
  )

  val convertF: ResultSet => SimpleDictRow = rs => SimpleDictRow(
    rs.getInt("id"),
    rs.getString("name")
  )

  def isDefaultSelected(defSelectedId: Option[Int], currId: Int): String =
    defSelectedId match {
      case Some(defSelId) =>
        if (defSelId == currId)
          " selected "
        else
          " "
      case None => " "
    }

  def selectSingleMaxPeriod(currPeriodId: Int, maxPeriodId: Int): String =
    if (currPeriodId == maxPeriodId)
      " selected "
    else
      " "

  def dictToHtmlCombo(d: SimpleDictData): String =
    s"""
       |<div id="div_combo_${d.dictMeta.dictCode}">${d.dictMeta.dictName}</br>
       |  <select ${d.dictMeta.selectType.getHtml}
       |  size=${if (d.rows.size <=3) 3 else 5}
       |  name="sel_${d.dictMeta.dictCode}" id="${d.dictMeta.dictCode}-select" width="240px">
       |  ${d.rows.map{r =>
             s"<option value=\"${r.id}\" " +
               s"${isDefaultSelected(d.dictMeta.defSelectedId,r.id)}  " +
               s"${ if (d.dictMeta.dictCode=="period") selectSingleMaxPeriod(r.id,d.rows.map(_.id).max) else " "}>${r.name}</option>"
            }.mkString}
       | </select>
       |</div>
       |""".stripMargin

  def newEmptyMainPage(): ZIO[AppConfig with ClickhouseService, Throwable, Response] = for {
    chService <- ZIO.service[ClickhouseService]
    appConfig <- ZIO.service[AppConfig]
    //List of effects to retrieve data for dictionaries from db.
    dictsEffects = reportDictionaries.map(d =>
      chService.getSimpleDict(
      dictMeta = d,
      f = convertF
    ))
    dicts <- ZIO.collectAllPar(dictsEffects).withParallelism(appConfig.nThreads)
    allDictsCombo <- ZIO.foreach(dicts){d => ZIO.succeed(dictToHtmlCombo(d))}
    mainPageContent <- ZIO.succeed(PageHtml.pageHtml(allDictsCombo.mkString))
    response <- ZIO.succeed(Response(Status.Ok,Headers("Content-Type", "text/html"),Body.fromString(mainPageContent)))
  } yield response

  def getTableHeaderHtml(reqParams: ReqReport1) :ZIO[ClickhouseService,SQLException,String] = for {
    chService <- ZIO.service[ClickhouseService]
    header <- chService.getReportHeader(reportId)
    headerHtml = header.map {
      h => s"<td bgcolor=\"#D3D3D3\">${h.name}</td>"
    }.mkString
  } yield s"<tr>$headerHtml</tr>"

/*  def getDataRows(reqParams: ReqReport1) :ZIO[ClickhouseService,SQLException,String] = for {
    chService <- ZIO.service[ClickhouseService]
    data <- chService.getReportData1(reportId,reqParams)
    rows = data.map {
      h =>
        s"""<tr>
           |<td>${h.rn}</td>
           |<td>${h.omsu_name}</td>
           |<td>${h.org_name}</td>
           |<td>${h.ogrn}</td>
           |<td>${h.ls_number}</td>
           |<td>${h.acc_status}</td>
           |<td>${h.gis_error}</td>
           |<td>${h.pd_label}</td>
           |<td>${h.gis_unique_number}</td>
           |<td>${h.pd_status}</td>
           |<td>${h.pd_gis_error}</td>
           |<td>${h.address}</td>
           |<td>${h.total}</td>
           |</tr>
           |""".stripMargin
    }.mkString
  } yield rows*/

/*  def getReport1DataHtml(reqParams: ReqReport1): ZIO[AppConfig with ClickhouseService, Throwable, Response] =
    for {
     tableHeader <- getTableHeaderHtml(reqParams)
     dataRows <- getDataRows(reqParams)
     table <- ZIO.succeed(s"<table border=2px solid; border-collapse: collapse;>$tableHeader$dataRows</table>")
     response <- ZIO.succeed(Response(Status.Ok,Headers("Content-Type", "text/html"),Body.fromString(table)))
  } yield response*/

  import comm.EncDecReport1JsonTemplate._
  def getReport1DataJson(reqParams: ReqReport1): ZIO[AppConfig with ClickhouseService, Throwable, Response] =
    for {
      chService <- ZIO.service[ClickhouseService]
      header <- chService.getReportHeader(reportId)
      cacheKey = reqParams.hashCode()
      sCheckCacheEx  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      isCacheExists <- chService.isCacheExistsCh(cacheKey)
      fCheckCacheEx  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- ZIO.logInfo(s"getReport1DataJson isCacheExists = $isCacheExists, ${fCheckCacheEx - sCheckCacheEx} ms")

      start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      headInfo <- chService.getReport1HeadInfo(reportId,reqParams,isCacheExists,cacheKey)
      middle <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- ZIO.logInfo(s"getReport1HeadInfo ${middle-start} ms.")

      data <- chService.getReportData1(reportId,reqParams,isCacheExists,cacheKey)
      finish  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- ZIO.logInfo(s"getReportData1 ${finish-middle} ms.")

      summary = SummaryReport1(
        execFetchSummaryMs = middle - start,
        execFetchDataMs = finish - middle,
        totalRows = data.headOption.map(_.total).getOrElse(0),
        currPage = reqParams.page_num,
        pageCnt = reqParams.page_cnt
      )
      response <- ZIO.succeed(Response.json(Report1JsonTemplate(summary,headInfo,header,data).toJson))
    } yield response

  private def makeWorkbookGetHeader(workbook: XSSFWorkbook, header: List[ReportHeaderRow]) :ZIO[Any,Throwable,XSSFSheet] = for {
    h <- ZIO.attemptBlockingInterrupt{
      val sheet = workbook.createSheet("ЛсПд")
      val headerStyle = workbook.createCellStyle()
      val headerRow = sheet.createRow(0)
      header.foldLeft(0){
        case (r,c) =>
          headerRow.createCell(r).setCellValue(c.name)
          r+1
      }
      sheet.setColumnWidth(1, 25 * 256)
      sheet.setColumnWidth(2, 45 * 256)
      sheet.setColumnWidth(3, 25 * 256)
      sheet.setColumnWidth(4, 25 * 256)
      sheet.setColumnWidth(5, 25 * 256)
      sheet.setColumnWidth(10, 45 * 256)
      sheet
    }
  } yield h

  private def updateExportEntity(key: Int, currRowsExported: Int): ZIO[ExportExcelCache, Nothing, Unit] = for {
    expCache <- ZIO.service[ExportExcelCache]
    expEntity <- expCache.lookup(key)
    newExpEntity = expEntity.map(ee => ee.copy(rowsExported = currRowsExported))
    _ <- expCache.set(key,newExpEntity)
  } yield ()

  private def populateSheet(key: Int, sheet: XSSFSheet, data: List[Report1Row]): ZIO[ExportExcelCache,Throwable,Unit] = for {
    rowsDone <- ZIO.foldLeft(data)(1){
      case (r,c) =>
        val row = sheet.createRow(r)
        row.createCell(0).setCellValue(c.rn)
        row.createCell(1).setCellValue(c.omsu_name)
        row.createCell(2).setCellValue(c.org_name)
        row.createCell(3).setCellValue(c.ogrn)
        row.createCell(4).setCellValue(c.ls_number)
        row.createCell(5).setCellValue(c.acc_status)
        row.createCell(6).setCellValue(c.gis_error)
        row.createCell(7).setCellValue(c.pd_label)
        row.createCell(8).setCellValue(c.pd_status)
        row.createCell(9).setCellValue(c.pd_gis_error)
        row.createCell(10).setCellValue(c.address)
        updateExportEntity(key, r).when(r % updateCacheEntityAfterRowCount == 0).as(r + 1)
    }
    _ <- updateExportEntity(key, rowsDone)
  } yield ()

  private def exportIntoExcelFile(@nowarn reqParams: ReqReport1,
                                  @nowarn fn: String,
                                  start: Long):
  ZIO[AppConfig with ClickhouseService with ExportExcelCache, Throwable, Unit] = for {
   _ <- ZIO.logInfo("Export begin ..................")
   conf <- ZIO.service[AppConfig]
   exportCache <- ZIO.service[ExportExcelCache]
   count <- exportCache.count
   _ <- ZIO.logInfo(s"Now in export cache $count elements.")
   key = reqParams.hashCode()
   _ <- ZIO.logInfo(s"export entity key = $key")

   chService <- ZIO.service[ClickhouseService]
   header <- chService.getReportHeader(reportId)
   data <- chService.getReportData1(reportId,reqParams, 0,key, 1)
   workbook <- ZIO.succeed(new XSSFWorkbook())
   _ <- exportCache.add(key,SingleExcelExportEntity(beginTS = start,knownTotalRows = data.head.total,excelFileName = Some(fn),exportStatus = excel.Process))
   sheet <- makeWorkbookGetHeader(workbook,header)
   _ <- populateSheet(key, sheet, data)
   fnFullName <- ZIO.succeed(s"${conf.excelPath}$fn")
   _ <- ZIO.logInfo(s"excel_file : $fnFullName")
   _ <- ZIO.attemptBlockingInterrupt{
     val outputStream = new FileOutputStream(fnFullName) // move up and write inside
     workbook.write(outputStream)
   }
   count <- exportCache.count
   _ <- ZIO.logInfo(s"export entity cache size = $count")
   c <- exportCache.lookup(key)
   time <- Clock.currentTime(TimeUnit.MILLISECONDS)
   entityForUpdate = c.map(e => e.copy(endTs = time,progressPercent = 100,exportStatus = Success))
   _ <- exportCache.set(key,entityForUpdate)

   elements <- exportCache.getAll()
   _ <- ZIO.foreachDiscard(elements){
     e => ZIO.logInfo(s"key = ${e._1}  ${e._2.excelFileName} rowsExported : ${e._2.rowsExported}")
   }
   expEntity <- exportCache.lookup(key)
    _ <- ZIO.logInfo(s"export entity cache size = $count thisExport rows = ${expEntity.map(_.rowsExported)}")
  } yield ()

  private def requestToEntity[A](r: Request)(implicit decoder: JsonDecoder[A]): ZIO[Any, Nothing, Either[String, A]] = for {
    req <- r.body
      .asString
      .map(_.fromJson[A])
      .catchAllDefect { case e: Exception =>
        ZIO
          .logError(s"Error[3] parsing input file with : ${e.getMessage}")
          .as(Left(e.getMessage))
      }
      .catchAll { case e: Exception =>
        ZIO
          .logError(s"Error[4] parsing input file with : ${e.getMessage}")
          .as(Left(e.getMessage))
      }
  } yield req

  import EncDecReqReport._
  def getReport(req: Request): ZIO[AppConfig with ClickhouseService, Throwable, Response] = for {
    debugReq <- req.body.asString
    _ <- ZIO.logInfo(s"req $debugReq")
    reqEntity <- requestToEntity[ReqReport1](req)
    response <- reqEntity match {
      case Left(errorString) => ZioResponseMsgBadRequest(errorString)
      case Right(report1Request) => getReport1DataJson(report1Request)
        //getReport1DataHtml(report1Request)
    }
  } yield response

  def exportExcel(req: Request): ZIO[AppConfig with ClickhouseService with ExportExcelCache, Throwable, Response] = for {
    debugReq <- req.body.asString
    _ <- ZIO.logInfo(s"req $debugReq")
    reqEntity <- requestToEntity[ReqReport1](req)
    tsStartExport <- Clock.currentTime(TimeUnit.MILLISECONDS)
    exportCache <- ZIO.service[ExportExcelCache]
    response <- reqEntity match {
      case Left(errorString) => ZioResponseMsgBadRequest(errorString)
      case Right(report1Request) =>
        ZIO.succeed(s"${TsToString(tsStartExport,"dd_MM_yyyy___HH_mm_ss")}.xlsx").flatMap { fn =>
            ZIO.ifZIO(exportCache.notExist(report1Request.hashCode()))(
              ZIO.logInfo(s"For key=${report1Request.hashCode()} do export from db.") *>
              exportIntoExcelFile(report1Request, fn, tsStartExport)
                .forkDaemon
                .as(Response.json(ExportSuccessStarted(report1Request.hashCode(), tsStartExport, fn).toJson)),
              ZIO.logInfo(s"For key=${report1Request.hashCode()} already exported.") *>
              exportCache.lookup(report1Request.hashCode()).flatMap {
                  case Some(s) =>
                    ZIO.succeed(Response.json(ExportSuccessStarted(report1Request.hashCode(), tsStartExport, s.excelFileName.getOrElse("[1] error in export cache")).toJson))
                  case None =>
                    ZioResponseMsgBadRequest("[2] error in export cache")
              }
          )
        }
    }
  } yield response

  import excel.EncDecExportProgress._
  def exportExcelPercent(id: Int, key: Int): ZIO[ExportExcelCache, Throwable, Response] = for {
    _ <- ZIO.logInfo(s"exportExcelPercent for report = $id")
    exportCache <- ZIO.service[ExportExcelCache]
    entity <- exportCache.lookup(key)
    status <- ZIO.succeed(entity.map(e => ExportProgress(e.progressPercent,e.exportStatus.toString)).getOrElse(ExportProgress(0,Wait.toString)))
    response <- ZIO.succeed(Response.json(status.toJson))
  } yield response

}
