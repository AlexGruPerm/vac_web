package rep1

import comm.ReqReport1
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object EncDecReqReport {

  implicit val encoderReqReport: JsonEncoder[ReqReport1] = DeriveJsonEncoder.gen[ReqReport1]
  implicit val decoderReqReport: JsonDecoder[ReqReport1] = DeriveJsonDecoder.gen[ReqReport1]

}
