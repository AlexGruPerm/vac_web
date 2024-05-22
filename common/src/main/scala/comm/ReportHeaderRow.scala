package comm

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

final case class ReportHeaderRow(id: Int, pid: Int, name: String, field_name: String, order_by: Int)

