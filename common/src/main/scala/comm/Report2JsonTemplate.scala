package comm

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Report2JsonTemplate(
                                header:   List[Report2HeaderRow],
                                data:     List[Report2Row]
                              )

object EncDecReport2JsonTemplate{

  implicit val encoderReport2HeaderRow: JsonEncoder[Report2HeaderRow] = DeriveJsonEncoder.gen[Report2HeaderRow]
  implicit val decoderReport2HeaderRow: JsonDecoder[Report2HeaderRow] = DeriveJsonDecoder.gen[Report2HeaderRow]

  implicit val encoderReport2Row: JsonEncoder[Report2Row] = DeriveJsonEncoder.gen[Report2Row]
  implicit val decoderReport2Row: JsonDecoder[Report2Row] = DeriveJsonDecoder.gen[Report2Row]

  implicit val encoderReport2JsonTemplate: JsonEncoder[Report2JsonTemplate] = DeriveJsonEncoder.gen[Report2JsonTemplate]
  implicit val decoderReport2JsonTemplate: JsonDecoder[Report2JsonTemplate] = DeriveJsonDecoder.gen[Report2JsonTemplate]

}