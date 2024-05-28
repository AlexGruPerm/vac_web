package comm

case class SummaryReport2(
                           execFetchSummaryMs: Long,
                           execFetchDataMs: Long,
                           totalRows: Int,
                           currPage: Int,
                           pageCnt: Int
                         )
