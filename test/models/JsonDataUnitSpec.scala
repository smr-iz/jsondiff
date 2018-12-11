package models

import java.util.UUID

import anorm.Row
import common.concurrency.CommonError
import helper.BaseSpec
import models.mysql.MySQL
import org.joda.time.DateTime
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.specs2.mock.Mockito

import scala.concurrent.duration._
/**
  * Created by semir on 10.12.2018.
  */
class JsonDataUnitSpec extends BaseSpec {
  "JsonData" should {
    "insertOrUpdate" should {
      "fail when there is an exception on DB" in new TestFragment {
        mockMySQL.executeUpdate(any, any) throws  new RuntimeException("MOCK EXCEPTION")
        val error: CommonError = jsonDataObj.insertOrUpdate(JsonData("1", None, None))
        error.errorFound mustBe true
      }

      "successfully insert" in new TestFragment {
        mockMySQL.executeUpdate(any, any) returns 1
        val error: CommonError = jsonDataObj.insertOrUpdate(JsonData("1", None, None))
        error.errorFound mustBe false
      }
    }

    "getJsDataById" should {
      "fail when there is an exception on DB" in new TestFragment {
        mockMySQL.apply(any, any) throws  new RuntimeException("MOCK EXCEPTION")
        val errorOrData: Either[CommonError, Option[JsonData]] = jsonDataObj.getJsDataById("1")
        errorOrData.isLeft mustBe true
      }

      "not found on DB" in new TestFragment {
        mockMySQL.apply(any, any) returns List.empty[Row]
        val errorOrData: Either[CommonError, Option[JsonData]] = jsonDataObj.getJsDataById("1")
        errorOrData.isRight mustBe true
        errorOrData.right.get mustBe None
      }

      "successfully get" in new TestFragment {
        val data = JsonData(UUID.randomUUID().toString, Option("eyJhIjoiYiJ9"), Option("eyJhIjoiYiJ9"))
        val mockRow = mock[Row]

        mockRow[String]("JsonData.id") returns data.id
        mockRow[String]("JsonData.right") returns data.right.get
        mockRow[String]("JsonData.left")returns data.left.get
        mockRow[DateTime]("JsonData.udate") returns data.uDate
        mockRow[DateTime]("JsonData.cDate") returns data.cDate

        mockMySQL.apply(any, any) returns List(mockRow)
        val errorOrData: Either[CommonError, Option[JsonData]] = jsonDataObj.getJsDataById("1")
        errorOrData.isRight mustBe true
        errorOrData.right.get.isDefined mustBe true
      }
    }
  }

  trait TestFragment extends Mockito with Matchers with ScalaFutures {
    val timeout = 30.seconds
    val mockMySQL = mock[MySQL]
    val jsonDataObj = spy(new JsonDataObj(mockMySQL))
  }
}


