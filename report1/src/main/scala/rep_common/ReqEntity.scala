package rep_common

import zio.ZIO
import zio.http.Request
import zio.json.{DecoderOps, JsonDecoder}

object ReqEntity {

  def requestToEntity[A](r: Request)(implicit decoder: JsonDecoder[A]): ZIO[Any, Nothing, Either[String, A]] = for {
    req <- r.body
      .asString
      .map(_.fromJson[A])
      .catchAllDefect { case e: Exception =>
        ZIO
          .logError(s"Error[3] parsing input file with : ${e.getMessage}")
          .as(Left(e.getMessage))
      }
      .catchAll { case e: Exception =>
        ZIO
          .logError(s"Error[4] parsing input file with : ${e.getMessage}")
          .as(Left(e.getMessage))
      }
  } yield req

}
