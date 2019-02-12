package services

import mapping.memmodel.ResponseObject
import mapping.request.{RunArchive, CheckFolderSize}

import scala.concurrent.Future

trait RunService {
  /** archive
    *
    * @param run  [[RunArchive]]
    * @return
    */
  def archive(run: RunArchive): Future[ResponseObject]

  /** check folder size
    *
    * @param dirPath  [[mapping.request.CheckFolderSize]]
    * @return
    */
  def checkFolderSize(dirPath: CheckFolderSize): Future[ResponseObject]
}