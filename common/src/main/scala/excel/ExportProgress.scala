package excel

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ExportProgress(percent: Int, status: String)

import excel.EncDecExportStatus._
object EncDecExportProgress {

/*  implicit val encoderExportStatus: JsonEncoder[ExportStatus] = DeriveJsonEncoder.gen[ExportStatus]
  implicit val decoderExportStatus: JsonDecoder[ExportStatus] = DeriveJsonDecoder.gen[ExportStatus]
*/
/*  implicit val encoderSingleExcelExportEntity: JsonEncoder[SingleExcelExportEntity] = DeriveJsonEncoder.gen[SingleExcelExportEntity]
  implicit val decoderSingleExcelExportEntity: JsonDecoder[SingleExcelExportEntity] = DeriveJsonDecoder.gen[SingleExcelExportEntity]
*/
  implicit val encoderExportProgress: JsonEncoder[ExportProgress] = DeriveJsonEncoder.gen[ExportProgress]
  implicit val decoderExportProgress: JsonDecoder[ExportProgress] = DeriveJsonDecoder.gen[ExportProgress]

}
