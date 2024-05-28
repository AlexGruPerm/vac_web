package rep1

import comm.{ReqReport1, ReqReport2}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object EncDecReqReport {

  implicit val encoderReqReport1: JsonEncoder[ReqReport1] = DeriveJsonEncoder.gen[ReqReport1]
  implicit val decoderReqReport1: JsonDecoder[ReqReport1] = DeriveJsonDecoder.gen[ReqReport1]

  implicit val encoderReqReport2: JsonEncoder[ReqReport2] = DeriveJsonEncoder.gen[ReqReport2]
  implicit val decoderReqReport2: JsonDecoder[ReqReport2] = DeriveJsonDecoder.gen[ReqReport2]

}
