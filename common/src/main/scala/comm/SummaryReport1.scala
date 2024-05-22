package comm

case class SummaryReport1(
                           execFetchSummaryMs: Long,
                           execFetchDataMs: Long,
                           totalRows: Int,
                           currPage: Int,
                           pageCnt: Int
                         )
