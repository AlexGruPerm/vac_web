package comm

import zio.{ZIO, ZIOAppArgs, ZLayer}
import com.typesafe.config.{Config, ConfigFactory}
import java.io
import java.io.File

case object ConfigHelper {

  def getConfig(fileConfig: Config): AppConfig = {
    val (webui) = ("webui.")
    val (clickhouse) = ("clickhouse.")
    AppConfig(
      fileConfig.getInt(webui + "port"),
      fileConfig.getInt(webui + "nThreads"),
      fileConfig.getString(webui + "excelPath"),
      ClickhouseConfig(
        fileConfig.getString(clickhouse + "ip"),
        fileConfig.getInt(clickhouse + "port"),
        fileConfig.getString(clickhouse + "db"),
        fileConfig.getString(clickhouse + "user"),
        fileConfig.getString(clickhouse + "password")
      )
    )
  }

  val config: ZIO[String, Exception, AppConfig] =
    for {
      configParam <- ZIO.service[String]
      configFilename: String = System.getProperty("user.dir") + File.separator + configParam
      fileConfig = ConfigFactory.parseFile(new io.File(configFilename))
      appConfig = ConfigHelper.getConfig(fileConfig)
    } yield appConfig

  def ConfigZLayer(confParam: ZIOAppArgs): ZIO[Any, Exception, ZLayer[Any, Exception, AppConfig]] = for {
    _ <- ZIO.fail(new Exception("Empty parameters. Please provide input config file."))
      .when(confParam.getArgs.isEmpty)
    appCfg = ZLayer {
      for {
        cfg <- confParam.getArgs.toList match {
          case List(configFile) => config.provide(ZLayer.succeed(configFile))
          case _ => ZIO.fail(new Exception("Empty parameters. Please provide input config file."))
        }
      } yield cfg
    }
  } yield appCfg

}
