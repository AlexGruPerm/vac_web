package excel

import excel.EncDecExportStatus.encoderExportStatus
import zio.json.internal.Write
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

sealed trait ExportStatus{
  def toString: String
}
case object Wait extends ExportStatus{
  override def toString: String = "Wait"
}
case object Process extends ExportStatus{
  override def toString: String = "Process"
}
case object Fail extends ExportStatus{
  override def toString: String = "Fail"
}
case object Success extends ExportStatus{
  override def toString: String = "Success"
}

object EncDecExportStatus {

  implicit val encoderExportStatus: JsonEncoder[ExportStatus] = DeriveJsonEncoder.gen[ExportStatus]
  implicit val decoderExportStatus: JsonDecoder[ExportStatus] = DeriveJsonDecoder.gen[ExportStatus]

}
