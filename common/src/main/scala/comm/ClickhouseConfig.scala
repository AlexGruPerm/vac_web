package comm

case class ClickhouseConfig(
                             ip: String,
                             port: Int,
                             db: String,
                             user: String,
                             password: String
                           ) {
  def getUrl(): String = s"jdbc:clickhouse:http://$ip:$port/$db"
}
