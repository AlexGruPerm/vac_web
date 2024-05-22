package comm

import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date


object TimestampConverter {

  def zo = ZoneOffset.ofHours(+3)

  def convertLongToDate(l: Long): Date = new Date(l)

  //http://tutorials.jenkov.com/java-internationalization/simpledateformat.html
  // Pattern Syntax
  //val DATE_FORMAT = "dd.MM.yyyy HH:mm:ss"

  /**
   * When we convert unix_timestamp to String representation of date and time is using same TimeZone.
   * Later we can adjust it with :
   *
   * val format = new SimpleDateFormat()
   * format.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"))
   * val dateAsString = format.format(date)
   *
   */
  def getDateAsString(d: Date, format: String): String = {
    val dateFormat = new SimpleDateFormat(format)
    dateFormat.format(d)
  }

  def TsToString(ts :Long, format: String = "dd.MM.yyyy HH:mm:ss") :String =
    getDateAsString(convertLongToDate(ts),format)

}