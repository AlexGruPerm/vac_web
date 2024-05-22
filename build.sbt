name := "vac_web"

ThisBuild / organization := "yakushev"
ThisBuild / version      := "0.0.1"
ThisBuild / scalaVersion := "2.13.10"
maintainer := "ugr@bk.ru"

val Versions = new {
  val ch_http_client     = "0.5.0"
  val apache_http_client = "5.3"
  val clickhouseJdbc     = "0.6.0"
  val slf4jApi           = "2.0.9"
  val log4jVers          = "2.0.9"
  val lz4Vers            = "1.8.0"
  val zio                = "2.0.20"
  val zio_config         = "4.0.0-RC16"
  val zio_http           = "3.0.0-RC6"
  val zio_json           = "0.6.2"
  val apacheDbcp2        = "2.12.0"
  val poi                = "5.2.5"
}

  // PROJECTS
  lazy val global = project
  .in(file("."))
    .enablePlugins(
      JavaAppPackaging,
      UniversalPlugin
    )
    .settings(
      Compile / mainClass := Some("webui.application.MainApp"),
      commonSettings)
  .aggregate(
    common,
    clickhouse,
    report1,
    webui
  )

  /**
   * Common classes.
   */
    lazy val common = (project in file("common"))
    .settings(
      name := "common",
      commonSettings,
      libraryDependencies ++= commonDependencies
    )

  /**
   * Common db functionality (Clickhouse)
  */
  lazy val clickhouse = (project in file("clickhouse"))
    .settings(
      name := "clickhouse",
      commonSettings,
      libraryDependencies ++= clickhouseDependencies
    ).dependsOn(common)

  /**
   * Report1 (extract data from db, html)
  */
  lazy val report1 = (project in file("report1"))
  .settings(
    name := "report1",
    libraryDependencies ++= commonDependencies ++ excelDependencies
  ).dependsOn(clickhouse,common)


  /**
   * Main application as enter point for reports.
   */
  lazy val webui = (project in file("webui"))
    .enablePlugins(
      JavaAppPackaging,
      UniversalPlugin
    )
    .settings(
      name := "webui",
      maintainer := "YakushevAN <ugr@bk.ru>",
      //Compile / mainClass := Some("vac_web.webui.application.MainApp"),
      Universal / mappings += {
        val jar = (Compile / packageBin).value
        jar -> ("lib/" + jar.getName)
      },
      commonSettings,
      libraryDependencies ++= commonDependencies ++ logDependencies,
    ).dependsOn(clickhouse,report1,common)


lazy val dependencies =
  new {
    val apacheHttpClient  = "org.apache.httpcomponents.client5" % "httpclient5" % Versions.apache_http_client
    val chHttpClient      = "com.clickhouse" % "clickhouse-http-client" % Versions.ch_http_client
    val ch                = "com.clickhouse" % "clickhouse-jdbc" % Versions.clickhouseJdbc classifier "all"
    val slf4j             = "org.slf4j" % "slf4j-api" % Versions.slf4jApi
    val log4j             = "org.slf4j" % "slf4j-log4j12" % Versions.log4jVers
    val lz4               = "org.lz4" % "lz4-java" % Versions.lz4Vers
    val zio               = "dev.zio" %% "zio" % Versions.zio
    val zio_conf          = "dev.zio" %% "zio-config" % Versions.zio_config
    val zio_conf_typesafe = "dev.zio" %% "zio-config-typesafe" % Versions.zio_config
    val zio_conf_magnolia = "dev.zio" %% "zio-config-magnolia" % Versions.zio_config
    val zio_http          = "dev.zio" %% "zio-http" % Versions.zio_http
    //val zio_json          = "dev.zio" %% "zio-json" % Versions.zio_json
    val dbcp2             = "org.apache.commons" % "commons-dbcp2" % Versions.apacheDbcp2
    val poi               = "org.apache.poi" % "poi" % Versions.poi
    val poi_ooxml         = "org.apache.poi" % "poi-ooxml" % Versions.poi

    val zioDep = List(zio)
    val common = List(zio, zio_conf, zio_conf_typesafe, zio_conf_magnolia, zio_http)
    val db = List(apacheHttpClient, chHttpClient,ch,slf4j, log4j, lz4, dbcp2)
    val logs = List(slf4j, log4j, lz4)
    val excel = List(poi,poi_ooxml)
  }

  lazy val logDependencies = {
    dependencies.logs
  }

  lazy val commonDependencies = {
    dependencies.common
  }

  lazy val clickhouseDependencies = {
    dependencies.db ++ dependencies.zioDep
  }

  lazy val excelDependencies = {
    dependencies.excel
  }

  lazy val compilerOptions = Seq(
    "-deprecation",
    "-encoding","UTF-8",
    "-explaintypes",
    "-feature",
    "-unchecked",
    "-language:postfixOps",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Ywarn-unused:params,-implicits"
  )

  lazy val commonSettings = Seq(
    scalacOptions ++= compilerOptions,
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
      Resolver.DefaultMavenRepository,
      Resolver.mavenLocal,
      Resolver.bintrayRepo("websudos", "oss-releases")
    )++
      Resolver.sonatypeOssRepos("snapshots")
      ++ Resolver.sonatypeOssRepos("public")
      ++ Resolver.sonatypeOssRepos("releases")
  )