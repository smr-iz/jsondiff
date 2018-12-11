package services

import common.concurrency.CommonError
import javax.inject.{Inject, Singleton}
import models._

/**
  * Created by semir on 8.12.2018.
  */
@Singleton
class JsonService @Inject()(JsonDataObj: JsonDataObj){

  def saveJsData(id: String, jsType: JsonType, dataToSave: String): CommonError = {
    val jsonDataResult: Either[CommonError, Option[JsonData]] = JsonDataObj.getJsDataById(id)

    jsonDataResult match {
      case Left(error) => error
      case Right(jsonDataAsOpt) => {
        val newJsonData: JsonData = jsonDataAsOpt match {
          case Some(dbData) => {
            if (jsType == JsonType.Left) {
              dbData.left = Some(dataToSave)
              dbData
            } else {
              dbData.right = Some(dataToSave)
              dbData
            }
          }
          case None => {
            if (jsType == JsonType.Left) {
              JsonData(id, Some(dataToSave), None)
            } else {
              JsonData(id, None, Some(dataToSave))
            }
          }
        }

        JsonDataObj.insertOrUpdate(newJsonData)
      }
    }
  }

  def getJsData(id: String): Either[CommonError, Option[JsonData]] = {
    JsonDataObj.getJsDataById(id)
  }

  def getResult(leftAsOpt: Option[String], rightAsOpt: Option[String]): JsonResult = {
    //return missing part if one of left/right is empty
    if (leftAsOpt.isEmpty || rightAsOpt.isEmpty) {
      JsonResult.MissingPart
    } else {
      val left = leftAsOpt.get
      val right = rightAsOpt.get
      if (left.equals(right)) {
        JsonResult.Equal
      } else if (left.length != right.length) {
        JsonResult.DifferentLength
      } else {
        var offset = 0
        var diffs: List[Diff] = List.empty

        (left zip right).zipWithIndex.foreach {
          case((leftChar, rightChar), index) => {
            if (!leftChar.equals(rightChar)) {
              //
              if ((index - offset) == 0) {
                offset = index
              }

              if (index == right.size - 1) {
                diffs = Diff(offset, index - offset + 1) :: diffs
              }

            } else {
              //left and right chars are equal
              if ((index - offset) > 0) {
                diffs = Diff(offset, index - offset) :: diffs
              }

              offset = index +1
            }
          }
        }

        val result = JsonResult.Different
        result.diff = diffs
        result
      }
    }
  }
}
