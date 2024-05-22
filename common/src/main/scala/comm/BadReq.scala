package comm

import zio.ZIO
import zio.http.{Response, Status}
import zio.json.EncoderOps

object BadReq {
  def ZioResponseMsgBadRequest(message: String): ZIO[Any, Nothing, Response] =
    ZIO.succeed(Response.json(ResponseMessage(message).toJson).status(Status.InternalServerError))
}
