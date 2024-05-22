package ch

import com.clickhouse.jdbc.ClickHouseDataSource
import comm.{HeadInfo, Report1Row, ReportHeaderRow, ReqReport1, SimpleDictData, SimpleDictMeta, SimpleDictRow, TimestampConverter}
import zio.{Clock, ZIO, ZLayer, durationInt}
import comm.TimestampConverter.TsToString

import java.sql.{ResultSet, SQLException}
import java.util.concurrent.TimeUnit
import scala.annotation.nowarn

trait ClickhouseService{
  def getReportHeader(reportId: Int): ZIO[Any,SQLException,List[ReportHeaderRow]]
  def getReportData1(reportId: Int, reqParams: ReqReport1, isCacheExists: Int, cacheKey: Int, isExport: Int = 0): ZIO[Any,SQLException,List[Report1Row]]
  def getSimpleDict(dictMeta: SimpleDictMeta, f: ResultSet => SimpleDictRow): ZIO[Any,SQLException,SimpleDictData]
  def getReport1HeadInfo(reportId: Int, reqParams: ReqReport1,isCacheExists: Int,cacheKey: Int): ZIO[Any,SQLException,HeadInfo]
  def isCacheExistsCh(cacheKey: Int): ZIO[Any, SQLException, Int]
  def insertCacheData(cacheKey: Int, dataQuery: String): ZIO[Any, SQLException, Unit]
  def insertCacheSummary(cacheKey: Int, summaryQuery: String): ZIO[Any, SQLException, Unit]
}

case class ClickhouseServiceImpl(pool: ClickHouseDataSource) extends ClickhouseService {

  private def executeSelect(sqlSelect: String): ZIO[Any,SQLException,ResultSet] = for {
    resultSetHeader <- ZIO.attemptBlockingInterrupt {
      pool.getConnection
        .createStatement
        .executeQuery(sqlSelect)
    }.refineToOrDie[SQLException]
  } yield resultSetHeader

  def getSimpleDict(dictMeta: SimpleDictMeta, f: ResultSet => SimpleDictRow):
  ZIO[Any,SQLException,SimpleDictData]=
    for {
      start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      rs <- executeSelect(dictMeta.sqlSelect)
      listRows = Iterator.continually(rs).takeWhile(_.next()).map(f).toList
      simpleDict = SimpleDictData(dictMeta, listRows)
      finish  <- Clock.currentTime(TimeUnit.MILLISECONDS)
      _ <- ZIO.logInfo(s"[${TsToString(start)}] Dict ${dictMeta.dictCode} selected in ${finish-start} ms.")
    } yield simpleDict

  private def getReportHeaderResultSet(reportId: Int): ZIO[Any,SQLException,ResultSet] = for {
    resultSetHeader <- ZIO.attemptBlockingInterrupt {
        pool.getConnection
          .createStatement
          .executeQuery(
            s"""
               |select id,pid,name,field_name,order_by
               |  from data.report_header(id_report = $reportId)
               |""".stripMargin
          )
      }.refineToOrDie[SQLException]
  } yield resultSetHeader

  def getReportHeader(reportId: Int): ZIO[Any,SQLException,List[ReportHeaderRow]] = for {
    rs <- getReportHeaderResultSet(reportId)
    lstHeaderRows = Iterator.continually(rs).takeWhile(_.next()).map{r =>
      ReportHeaderRow(
        r.getInt("id"),
        r.getInt("pid"),
        r.getString("name"),
        r.getString("field_name"),
        r.getInt("order_by")
      )
    }.toList
  } yield lstHeaderRows

  private def getReportDataResultSet(@nowarn reportId: Int, reqParams: ReqReport1) : ZIO[Any,SQLException,ResultSet] = for {
    resultSetData <- ZIO.attemptBlockingInterrupt {
      val query =
        s"""
           |	select acc.omsu_name,                                        -- 1.
           |	       acc.org_name,                                         -- 2.
           |	       acc.ogrn,                                             -- 3.
           |	       acc.label as ls_number,                               -- 4.
           |	       acc.acc_status,                                       -- 5.
           |	       acc.gis_error,                                        -- 6.
           |	       coalesce(pd.label,'Not set') as pd_label,         -- 7.
           |         pd.gis_unique_number,
           |	       pd.pd_status,                                         -- 8.
           |	       pd.gis_error as pd_gis_error,                         -- 9.
           |	       acc.address,                                          -- 10.
           |	       rn,
           |	       total
           |	from
           |	      (
           |	        select a.*
           |	          from(
           |			   select ai.*,
           |			          row_number() over(order by omsu_name, org_name, address) as rn
           |		             ,sum(1) over()                                          as total
           |		        from data.accounts ai
           |		        where 1=1
                      ${
          if (reqParams.omsu.nonEmpty)
            s" and ai.id_omsu in (${reqParams.omsu.mkString(",")}) "
          else
            " "
        }
           |          ${if (reqParams.existpd == -1)
          " "
        else if (reqParams.existpd == 0)
          s" and  ai.id not in (select pd.id_account from data.account_payment_docs pd where pd.period = ${reqParams.period}) "
        else
          s" and  ai.id     in (select pd.id_account from data.account_payment_docs pd where pd.period = ${reqParams.period}) "
        }
                      ${if (reqParams.org.nonEmpty)
          s" and ai.id_voc_agent in (${reqParams.org.mkString(",")}) "
        else " "
        }
                       ${
          if (reqParams.errls == -1)
            " "
          else if (reqParams.errls == 0)
            " and ai.gis_error is null "
          else
            " and ai.gis_error is not null "
        }
           |	           ) a
           |	           where a.rn between ${(reqParams.page_num - 1)*reqParams.page_cnt + 1}
           |               and ${reqParams.page_num * reqParams.page_cnt}
           |		  ) acc
           |	left join (
           |		        select pdi.*
           |		          from data.account_payment_docs pdi
           |		         where pdi.period = ${reqParams.period}
           |		           and pdi.id_account in (
           |                                         select ai.id
           |                                           from data.accounts ai
           |                                          where 1=1
                                                     ${if (reqParams.omsu.nonEmpty)
          s" and ai.id_omsu in (${reqParams.omsu.mkString(",")}) "
        else
          " "
        }
                                                    ${if (reqParams.org.nonEmpty)
          s" and ai.id_voc_agent in (${reqParams.org.mkString(",")}) "
        else " "
        }
           |                                     )
           |	          ) pd
           |	       on acc.id = pd.id_account
           |	order by acc.rn
           |	settings join_use_nulls = 1
           |""".stripMargin
      //todo: DEBUG println(query)
      pool.getConnection
        .createStatement
        .executeQuery(query)
    }.refineToOrDie[SQLException]
  } yield resultSetData

  private def getReportDataResultSetView(@nowarn reportId: Int,
                                         reqParams: ReqReport1,
                                         isExport: Int = 0,
                                         isCacheExists: Int,
                                         cacheKey: Int) : ZIO[Any,SQLException,(ResultSet,Option[String])] = for {
    //_ <- ZIO.logInfo(s"cache for cacheKey = $cacheKey  EXIST : $isCacheExists  Check ${finish - start} ms.")
    resultSetDataOptQuery <-
      if (isCacheExists == 0) {
      ZIO.attemptBlockingInterrupt {
        val query =
          s"""
             |select *
             |from   data.v_report_1(
             |                       p_omsu      = [${reqParams.omsu.mkString(",")}]::Array(Int32),
             |                       p_orgs      = [${reqParams.org.mkString(",")}]::Array(Int32),
             |                       p_popup_err_ls = [${reqParams.errlstxt.map(elm => s"'$elm'").mkString(",")}]::Array(String),
             |                       p_exist_pd  = ${reqParams.existpd},
             |                       p_err_pd    = ${reqParams.errpd},
             |                       p_err_ls    = ${reqParams.errls},
             |                       p_period    = ${reqParams.period},
             |                       p_first     = ${(reqParams.page_num - 1) * reqParams.page_cnt + 1},
             |                       p_last      = ${
                                                      if (isExport == 0)
                                                        reqParams.page_num * reqParams.page_cnt
                                                      else
                                                        -1
                                                    }
             |                      )
             |                      """
            .stripMargin
        //todo: DEBUG println(query)
        val rs = pool.getConnection
          .createStatement
          .executeQuery(query)
        (rs,Some(query))
      }.refineToOrDie[SQLException]
    } else {
        ZIO.attemptBlockingInterrupt {
          val query =
            s"""
               |select rn,omsu_name,org_name,ogrn,ls_number,acc_status,gis_error,pd_label,
               |       gis_unique_number,pd_status,pd_gis_error,address,total
               |  from data.rep1_cache_data
               | where cache_key = $cacheKey
               | order by rn
               """.stripMargin
          //todo: DEBUG println(query)
          val rs = pool.getConnection
            .createStatement
            .executeQuery(query)
          (rs,None)
        }.refineToOrDie[SQLException]
      }
  } yield resultSetDataOptQuery

  /*
  val createCacheQuery: String =
    s""" create table rep1caches.cache_rep1_${if (key<0) "m" else ""}${key.abs}
       |  Engine=MergeTree
       |  Primary key(rn) as $dataQuery
       |  """.stripMargin
  */

  def insertCacheData(cacheKey: Int, dataQuery: String): ZIO[Any, SQLException, Unit] = for {
    start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _ <- ZIO.attemptBlockingInterrupt {
      /******************** CREATE NEW CACHE IN CLICKHOUSE *************************/
      val createCacheQuery: String =
        s"""insert into data.rep1_cache_data(
           |                                 cache_key,rn,omsu_name,org_name,ogrn,ls_number,acc_status,gis_error,
           |                                 pd_label,gis_unique_number,pd_status,pd_gis_error,address,total
           |                                )
           |select $cacheKey as cache_key,
           |       rn,omsu_name,org_name,ogrn,ls_number,acc_status,gis_error,pd_label,gis_unique_number,pd_status,pd_gis_error,address,total
           |  from($dataQuery)
           |  """.stripMargin
      println(createCacheQuery)
      pool.getConnection
        .createStatement
        .executeQuery(createCacheQuery)
      /*****************************************************************************/
    }.refineToOrDie[SQLException]
    finish  <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _ <- ZIO.logInfo(s"insert into rep1_cache_data, duration ${finish - start} ms.")
  } yield ()


  def insertCacheSummary(cacheKey: Int, summaryQuery: String): ZIO[Any, SQLException, Unit] = for {
    start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
    //_ <- ZIO.sleep(30.seconds)
    _ <- ZIO.attemptBlockingInterrupt {
      /******************** CREATE NEW CACHE IN CLICKHOUSE *************************/
      val createCacheQuery: String =
        s"""insert into data.rep1_cache_summary(cache_key,total_cnt_rows,cnt_no_pd,cnt_pd_error,cnt_acc_error)
           |select $cacheKey as cache_key,total_cnt_rows,cnt_no_pd,cnt_pd_error,cnt_acc_error
           |  from($summaryQuery)
           |  """.stripMargin
      println(createCacheQuery)
      pool.getConnection
        .createStatement
        .executeQuery(createCacheQuery)
      /*****************************************************************************/
    }.refineToOrDie[SQLException]
    finish  <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _ <- ZIO.logInfo(s"insert into rep1_cache_data, duration ${finish - start} ms.")
  } yield ()

  def getReportData1(reportId: Int, reqParams: ReqReport1, isCacheExists: Int,cacheKey: Int, isExport: Int = 0):
  ZIO[Any,SQLException,List[Report1Row]] = for {
    start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
    rs <- getReportDataResultSetView(reportId, reqParams, isExport, isCacheExists, cacheKey)
    //in separate thread execute create cache in db
    _ <- rs._2 match {
      case Some(q)  => insertCacheData(reqParams.hashCode(), q).forkDaemon.when(isExport == 0)
      case None => ZIO.unit
    }

    lstDataRows = Iterator.continually(rs._1).takeWhile(_.next()).map{r =>
      val lsErrorData = r.getString("gis_error")
      val lsError = if (r.wasNull())
        " " else
        lsErrorData

      val pdStatusData = r.getString("pd_status")
      val pdStatus = if (r.wasNull())
        " " else pdStatusData

      val pdErrorData = r.getString("pd_gis_error")
      val pdError = if (r.wasNull())
        " " else pdErrorData

      val gisUnqNumber = r.getString("gis_unique_number")
      val gisUnqNum = if (r.wasNull())
        " " else gisUnqNumber

        Report1Row(
        r.getString("omsu_name"),
        r.getString("org_name"),
        r.getString("ogrn"),
        r.getString("ls_number"),
        r.getString("acc_status"),
        lsError,
        r.getString("pd_label"),
          gisUnqNum,
        pdStatus,
        pdError,

        r.getString("address"),
        r.getInt("rn"),
        r.getInt("total")
      )

    }.toList
    finish  <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _ <- ZIO.logInfo(s" >>>>>>>>>>> getReportData1 duration ${finish-start} ms.")
  } yield lstDataRows

  private def getReport1HeadInfoResultSet(@nowarn reportId: Int,
                                          @nowarn reqParams: ReqReport1,
                                          isCacheExists: Int,
                                          cacheKey: Int): ZIO[Any,SQLException,(ResultSet,Option[String])] = for {
    resultSetDataOptQuery <-
      if (isCacheExists == 0)
         ZIO.attemptBlockingInterrupt {
          val query =
            s"""
               |select *
               |from   data.v_report_1_summary(
               |		                           p_omsu      = [${reqParams.omsu.mkString(",")}]::Array(Int32),
               |		                           p_orgs      = [${reqParams.org.mkString(",")}]::Array(Int32),
               |                               p_popup_err_ls = [${reqParams.errlstxt.map(elm => s"'$elm'").mkString(",")}]::Array(String),
               |                               p_err_pd    = ${reqParams.errpd},
               |		                           p_exist_pd  = ${reqParams.existpd},
               |		                           p_err_ls    = ${reqParams.errls},
               |		                           p_period    = ${reqParams.period}
               |		                          )
               |""".stripMargin
          //todo: DEBUG println(query)
          val rs = pool.getConnection
            .createStatement
            .executeQuery(query)
          rs.next()
          (rs, Some(query))
        }.refineToOrDie[SQLException]
      else {
        ZIO.attemptBlockingInterrupt {
          val query =
            s"""
               |select total_cnt_rows,cnt_no_pd,cnt_pd_error,cnt_acc_error
               |  from data.rep1_cache_summary
               | where cache_key = $cacheKey
               """.stripMargin
          //todo: DEBUG println(query)
          val rs = pool.getConnection
            .createStatement
            .executeQuery(query)
          rs.next()
          (rs,None)
        }.refineToOrDie[SQLException]
      }
  } yield resultSetDataOptQuery

  def getReport1HeadInfo(reportId: Int, reqParams: ReqReport1, isCacheExists: Int, cacheKey: Int): ZIO[Any,SQLException,HeadInfo] = for {
    rs <- getReport1HeadInfoResultSet(reportId, reqParams,isCacheExists, cacheKey)
    _ <- rs._2 match {
      case Some(q) => insertCacheSummary(cacheKey,q).forkDaemon
      case None => ZIO.unit
    }
    headInfo = HeadInfo(
      rs._1.getInt("total_cnt_rows"),
      rs._1.getInt("cnt_no_pd"),
      rs._1.getInt("cnt_pd_error"),
      rs._1.getInt("cnt_acc_error")
    )
  } yield headInfo

  def isCacheExistsCh(cacheKey: Int): ZIO[Any, SQLException, Int] = for {
    exist <- ZIO.attemptBlockingInterrupt {
        val rs       = pool.getConnection
          .createStatement
          .executeQuery(
            s"""
               | select if(   exists(select 1 from data.rep1_cache_data    d where d.cache_key = $cacheKey)
               |          and exists(select 1 from data.rep1_cache_summary s where s.cache_key = $cacheKey),1,0) as is_cache_exists
               |""".stripMargin
/*            s"""
               | select if(exists(
               |				          select 1
               |				            from system.tables t
               |				           where t.database = 'rep1caches' and
               |				                 t.name     = 'cache_rep1_${if (key<0) "m" else ""}${key.abs}'
               |                 ),1,0) as is_table_exists
               |""".stripMargin*/
          )
        rs.next()
        val resTblEx = rs.getInt(1)
        rs.close()
        resTblEx
      }.tapError(er => ZIO.logError(er.getMessage))
      .refineToOrDie[SQLException]
  } yield exist

}

object ClickhouseServiceImpl {
  val layer: ZLayer[ClickhousePool, SQLException, ClickhouseService] =
    ZLayer {
      for {
        pool <- ZIO.service[ClickhousePool]
        clickhouseDs <- pool.getClickHousePool()
      } yield ClickhouseServiceImpl(clickhouseDs)
    }
}
