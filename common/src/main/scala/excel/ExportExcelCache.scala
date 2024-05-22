package excel

import zio.{Ref, UIO, ZIO, ZLayer}

import scala.collection.immutable.IntMap
import scala.collection.mutable

trait ExportExcelCache {
  def getAll(): UIO[mutable.Map[Int, SingleExcelExportEntity]]
  def add(key: Int, entity: SingleExcelExportEntity): UIO[Unit]
  def lookup(key: Int): UIO[Option[SingleExcelExportEntity]]
  def count: UIO[Int]
  def set(key: Int, entity: Option[SingleExcelExportEntity]): UIO[Unit]
  def notExist(key: Int): UIO[Boolean]
}

case class ImplExportExcelCache(ref: Ref[mutable.Map[Int, SingleExcelExportEntity]]) extends ExportExcelCache {

  def getAll(): UIO[mutable.Map[Int, SingleExcelExportEntity]] =
    ref.get

  def add(key: Int, entity: SingleExcelExportEntity): UIO[Unit] =
    ref.update(m => m ++ IntMap(key -> entity))

  def lookup(key: Int): UIO[Option[SingleExcelExportEntity]] =
    ref.get.map(_.get(key))

  def count: UIO[Int] = ref.get.map(_.size)

  def set(key: Int, entity: Option[SingleExcelExportEntity]): UIO[Unit] =
    entity match {
      case Some(e) =>
        val currPercent = ((e.rowsExported*100/e.knownTotalRows)/10)*10
        ref.update(m => m ++ IntMap(key -> e.copy(progressPercent = currPercent))) *>
        ZIO.logInfo(s"cache entity for key=$key updated. rowsExported = ${e.rowsExported} currPercent = $currPercent")
      case None => ZIO.unit
    }

  def notExist(key: Int): UIO[Boolean] =
    ref.get.map(!_.contains(key))

}

object ImplExportExcelCache {
  def layer: ZLayer[Any, Nothing, ExportExcelCache] =
    ZLayer.fromZIO(
      Ref.make(mutable.Map.empty[Int, SingleExcelExportEntity]).map(new ImplExportExcelCache(_))
    )
}