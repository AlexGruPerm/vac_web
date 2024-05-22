package comm

final case class AppConfig(port: Int, nThreads: Int, excelPath: String, clickhouseConfig: ClickhouseConfig)