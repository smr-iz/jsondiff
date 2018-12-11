package models

import java.util.UUID

import anorm.{Row, SimpleSql}
import common.concurrency.CommonError
import helper.BaseSpec
import models.mysql.MySQL
import org.joda.time.DateTime

/**
  * Created by semir on 10.12.2018.
  */
class JsonDataIntSpec extends BaseSpec {
  val jsonDataObj = app.injector.instanceOf[JsonDataObj]
  val mySQL = app.injector.instanceOf[MySQL]

  "JsonData" should {
    val now = DateTime.now().secondOfMinute().roundHalfCeilingCopy()
    "insertOrUpdate" should {
      "successfully insert" in {
        val id = UUID.randomUUID().toString
        val dataForInsert = JsonData(id, Option("eyJhIjoiYiJ9"), Option("eyJhIjoiYiJ9"), now, now)
        val error: CommonError = jsonDataObj.insertOrUpdate(dataForInsert)
        error.errorFound mustBe false

        val selectSql = anorm.SQL(
          s"""
             |SELECT *
             |  FROM JsonData
             | WHERE id = {id}
         """.stripMargin).on("id" -> id)

        val savedData = mySQL.apply(selectSql).map(row => jsonDataObj.convertFromRow(row)).headOption
        savedData mustBe Some(dataForInsert)
      }
    }

    "getJsDataById" should {
      "not found on DB" in {
        val id = UUID.randomUUID().toString
        val errorOrData: Either[CommonError, Option[JsonData]] = jsonDataObj.getJsDataById(id)
        errorOrData.isRight mustBe true
        errorOrData.right.get mustBe None
      }

      "successfully get" in {
        val data = JsonData(UUID.randomUUID().toString, Option("eyJhIjoiYiJ9"), Option("eyJhIjoiYiJ9"))

        val insertOrUpdateSQL: SimpleSql[Row] = anorm.SQL(
          s"""
             |INSERT INTO JsonData (${jsonDataObj.fieldsAsString})
             |VALUES ${jsonDataObj.fieldsForValueTypeAsString}
           """.stripMargin
        ).on(jsonDataObj.getParametersForObject(data): _*)

        mySQL.executeUpdate(insertOrUpdateSQL)
        val errorOrData: Either[CommonError, Option[JsonData]] = jsonDataObj.getJsDataById(data.id)
        errorOrData.isRight mustBe true
        errorOrData.right.get.isDefined mustBe true
      }
    }
  }
}
