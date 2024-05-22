package ch

import com.clickhouse.jdbc.ClickHouseDataSource
import comm.ClickhouseConfig
import zio.{Clock, ZIO, ZLayer}

import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.TimeUnit

trait ClickhousePool {
  val props = new Properties()
  def getClickHousePool(): ZIO[Any, SQLException, ClickHouseDataSource]
}

case class ClickhousePoolImpl(ch: ClickhouseConfig) extends ClickhousePool {
  override def getClickHousePool(): ZIO[Any, SQLException, ClickHouseDataSource] = for {
    start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _      <-
      ZIO.logInfo(s"getClickHousePool............ ")
    sess   <- ZIO.attemptBlockingInterrupt {
      props.setProperty("http_connection_provider", "HTTP_URL_CONNECTION")
      //TODO: set correct values, property in ms.
      props.setProperty("connection_timeout", "10000000")
      props.setProperty("socket_timeout", "10000000")
      props.setProperty("dataTransferTimeout", "10000000")
      props.setProperty("timeToLiveMillis", "10000000")
      props.setProperty("socket_keepalive", "true")
      props.setProperty("http_receive_timeout", "10000000")
      props.setProperty("keep_alive_timeout", "10000000")
      props.setProperty("user", ch.user);
      props.setProperty("password", ch.password);
      props.setProperty("client_name", "orach");
      val dataSource: ClickHouseDataSource = new ClickHouseDataSource(ch.getUrl(), props)
      dataSource
    }.refineToOrDie[SQLException]
    finish <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _ <- ZIO.logDebug(s"getClickHousePool ${finish - start} ms.")
  } yield sess
}

object ClickhousePoolImpl {
  val layer: ZLayer[ClickhouseConfig, SQLException, ClickhousePool] =
    ZLayer {
      for {
        conf <- ZIO.service[ClickhouseConfig]
      } yield ClickhousePoolImpl(conf)
    }
}
