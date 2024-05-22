package comm

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Report1JsonTemplate(
                               summary:  SummaryReport1,
                               headInfo: HeadInfo,
                               header:   List[ReportHeaderRow],
                               data:     List[Report1Row]
                              )

object EncDecReport1JsonTemplate{
  implicit val encoderHeadInfo: JsonEncoder[HeadInfo] = DeriveJsonEncoder.gen[HeadInfo]
  implicit val decoderHeadInfo: JsonDecoder[HeadInfo] = DeriveJsonDecoder.gen[HeadInfo]

  implicit val encoderSummaryReport1: JsonEncoder[SummaryReport1] = DeriveJsonEncoder.gen[SummaryReport1]
  implicit val decoderSummaryReport1: JsonDecoder[SummaryReport1] = DeriveJsonDecoder.gen[SummaryReport1]

  implicit val encoderReportHeaderRow: JsonEncoder[ReportHeaderRow] = DeriveJsonEncoder.gen[ReportHeaderRow]
  implicit val decoderReportHeaderRow: JsonDecoder[ReportHeaderRow] = DeriveJsonDecoder.gen[ReportHeaderRow]

  implicit val encoderReport1Row: JsonEncoder[Report1Row] = DeriveJsonEncoder.gen[Report1Row]
  implicit val decoderReport1Row: JsonDecoder[Report1Row] = DeriveJsonDecoder.gen[Report1Row]

  implicit val encoderReport1JsonTemplate: JsonEncoder[Report1JsonTemplate] = DeriveJsonEncoder.gen[Report1JsonTemplate]
  implicit val decoderReport1JsonTemplate: JsonDecoder[Report1JsonTemplate] = DeriveJsonDecoder.gen[Report1JsonTemplate]
}
