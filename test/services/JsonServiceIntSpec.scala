package services

import java.util.UUID

import helper.BaseSpec
import models.mysql.MySQL
import models.{Diff, JsonDataObj, JsonResult, JsonType}

/**
  * Created by semir on 10.12.2018.
  */
class JsonServiceIntSpec extends BaseSpec {
  val jsonService = app.injector.instanceOf[JsonService]
  val jsonDataObj = app.injector.instanceOf[JsonDataObj]
  val mySQL = app.injector.instanceOf[MySQL]

  "saveJsData" should {
    "save left data" in {
      val id = UUID.randomUUID().toString
      val error = jsonService.saveJsData(id, JsonType.Left, "eyJhIjoiYiJ9")
      error.errorFound mustBe false

      val selectSql = anorm.SQL(
        s"""
           |SELECT *
           |  FROM JsonData
           | WHERE id = {id}
         """.stripMargin).on("id" -> id)

      val savedData = mySQL.apply(selectSql).map(row => jsonDataObj.convertFromRow(row)).headOption
      savedData.isDefined mustBe true
      savedData.get.id mustEqual id
    }

    "save right data" in {
      val id = UUID.randomUUID().toString
      val error = jsonService.saveJsData(id, JsonType.Right, "eyJhIjoiYiJ9")
      error.errorFound mustBe false

      val selectSql = anorm.SQL(
        s"""
           |SELECT *
           |  FROM JsonData
           | WHERE id = {id}
         """.stripMargin).on("id" -> id)

      val savedData = mySQL.apply(selectSql).map(row => jsonDataObj.convertFromRow(row)).headOption
      savedData.isDefined mustBe true
      savedData.get.id mustEqual id
    }
  }

  "getResult" should {
    "return MissingPart when left is empty" in {
      val result = jsonService.getResult(None, Some("test"))
      result mustEqual JsonResult.MissingPart
    }

    "return MissingPart when right is empty" in {
      val result = jsonService.getResult(Some("test"), None)
      result mustEqual JsonResult.MissingPart
    }

    "return Equal when left and right equal" in {
      val result = jsonService.getResult(Some("test"), Some("test"))
      result mustEqual JsonResult.Equal
    }

    "return DifferentLength when left and right have different size" in {
      val result = jsonService.getResult(Some("test"), Some("testsomething"))
      result mustEqual JsonResult.DifferentLength
    }

    "return Different with offset and length" in {
      val result = jsonService.getResult(Some("eyihIjoiYiJ9122a"), Some("abJhIjoiYiJ7211a"))
      result mustEqual JsonResult.Different
      result.diff mustEqual List(Diff(11,4), Diff(0,3))
    }
  }
}
