package excel

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class SingleExcelExportEntity(
                                   beginTS: Long = 0L,
                                   endTs:   Long = 0L,
                                   progressPercent: Int = 0, // 0..100
                                   rowsExported: Int = 0,
                                   knownTotalRows: Int = 0,
                                   excelFileName: Option[String] = Option.empty[String],
                                   exportStatus: ExportStatus = Wait,
                                   exportErrorMsg: Option[String] = Option.empty[String]
                                  )

