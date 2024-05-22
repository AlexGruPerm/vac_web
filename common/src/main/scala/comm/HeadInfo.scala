package comm

case class HeadInfo(
                     totalCntRows: Int = 0,
                     cntNoPd:      Int = 0,
                     cntPdError:   Int = 0,
                     cntAccError:  Int = 0
                   )
