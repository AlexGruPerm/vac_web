package excel

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ExportSuccessStarted(key: Int, timestampStart: Long, fileName: String)

object EncDecReport1JsonTemplate {
  implicit val encoderExportSuccessStarted: JsonEncoder[ExportSuccessStarted] = DeriveJsonEncoder.gen[ExportSuccessStarted]
  implicit val decoderExportSuccessStarted: JsonDecoder[ExportSuccessStarted] = DeriveJsonDecoder.gen[ExportSuccessStarted]
}