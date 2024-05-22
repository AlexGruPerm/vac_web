package comm

case class ReqReport1(
                       org:    List[Int] = List.empty[Int],
                       omsu:   List[Int] = List.empty[Int],
                       existpd:  Int = -1,
                       errpd:    Int = -1,
                       errls:    Int = -1,
                       period:   Int,
                       page_num: Int,
                       page_cnt: Int,
                       errlstxt: List[String] = List.empty[String]
                    ){
}
