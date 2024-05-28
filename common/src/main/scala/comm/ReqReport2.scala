package comm

case class ReqReport2(
                       org:    List[Int] = List.empty[Int],
                       omsu:   List[Int] = List.empty[Int],
                       existpd:  Int = -1,
                       //errpd:    Int = -1,
                       errls:    Int = -1,
                       nyear:   Int,
                       page_num: Int,
                       page_cnt: Int,
                       errlstxt: List[String] = List.empty[String]
                     ){
}