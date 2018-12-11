package controllers

import java.util.{Base64, UUID}

import anorm.{Row, SimpleSql}
import helper.BaseSpec
import models.mysql.MySQL
import models.{JsonData, JsonDataObj}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString

import scala.concurrent.duration._


/**
  * Created by semir on 10.12.2018.
  */
class JsonControllerFunSpec extends BaseSpec  with Status{
  val JsonController = app.injector.instanceOf[JsonController]
  val jsonDataObj = app.injector.instanceOf[JsonDataObj]
  val mySQL = app.injector.instanceOf[MySQL]
   val timeout = 30.seconds

  "set left" should {
    "fail with Missing encoded data" in {
      val id = UUID.randomUUID().toString
      val result = JsonController.setLeft(id)
        .apply(FakeRequest("POST", s"v1/diff/${id}/left"))
      contentAsString(result)(timeout) mustBe """{"error":{"default":{"MissingParameter":"encodedData"}}}"""
    }

    "successfully save left data" in {
      val jsData = Json.obj("test" -> "foo")
      val encodedData = Base64.getEncoder.encode(jsData.toString().getBytes()).toString
      val id = UUID.randomUUID().toString
      val result = JsonController.setLeft(id)
        .apply(FakeRequest("POST", s"v1/diff/${id}/left").withTextBody(encodedData))
      contentAsString(result)(timeout) mustBe """{"success":true}"""
    }
  }

  "set right" should {
    "fail with Missing encoded data" in {
      val id = UUID.randomUUID().toString
      val result = JsonController.setRight(id)
        .apply(FakeRequest("POST", s"v1/diff/${id}/left"))
      contentAsString(result)(timeout) mustBe """{"error":{"default":{"MissingParameter":"encodedData"}}}"""
    }

    "successfully save right data" in {
      val jsData = Json.obj("test" -> "foo")
      val encodedData = Base64.getEncoder.encode(jsData.toString().getBytes()).toString
      val id = UUID.randomUUID().toString
      val result = JsonController.setRight(id)
        .apply(FakeRequest("POST", s"v1/diff/${id}/left").withTextBody(encodedData))
      contentAsString(result)(timeout) mustBe """{"success":true}"""
    }
  }

  "get" should {
    "when not found data with given id" in {
      val id = UUID.randomUUID().toString
      val result = JsonController.get(id)
        .apply(FakeRequest("GET", s"v1/diff/${id}"))
      contentAsString(result)(timeout) mustBe """{"error":{"default":{"NotFound":"notFoundId"}}}"""
    }

    "when left and right equal" in {
      val savedJsData = saveAnInstance()
      val result = JsonController.get(savedJsData.id)
        .apply(FakeRequest("GET", s"v1/diff/${savedJsData.id}"))
      contentAsString(result)(timeout) mustBe """{"success":{"data":"eyJhIjoiYiJ9"}}"""
    }

    "when left and right size are different" in {
      val savedJsData = saveAnInstance(Some("eyJhIjoiYiJ9"), Some("eyJhIjoiYiJ911111"))
      val result = JsonController.get(savedJsData.id)
        .apply(FakeRequest("GET", s"v1/diff/${savedJsData.id}"))
      contentAsString(result)(timeout) mustBe """{"success":{"differentLength":true}}"""
    }

    "when left is empty" in {
      val savedJsData = saveAnInstance(None,  Some("eyJhIjoiYiJ911111"))
      val result = JsonController.get(savedJsData.id)
        .apply(FakeRequest("GET", s"v1/diff/${savedJsData.id}"))
      contentAsString(result)(timeout) mustBe """{"success":{"emptyPart":true}}"""
    }

    "when right is empty" in {
      val savedJsData = saveAnInstance(Some("eyJhIjoiYiJ911111"), None)
      val result = JsonController.get(savedJsData.id)
        .apply(FakeRequest("GET", s"v1/diff/${savedJsData.id}"))
      contentAsString(result)(timeout) mustBe """{"success":{"emptyPart":true}}"""
    }

    "when size equal return diff" in {
      val savedJsData = saveAnInstance(Some("eyJhIjoiYiJ911111"), Some("eyJKIjoiYiJ922222"))
      val result = JsonController.get(savedJsData.id)
        .apply(FakeRequest("GET", s"v1/diff/${savedJsData.id}"))
      contentAsString(result)(timeout) mustBe """{"success":{"diff":[{"offset":3,"length":1},{"offset":12,"length":5}]}}"""
    }
  }

  def saveAnInstance(left: Option[String] = Option("eyJhIjoiYiJ9"),
                     right: Option[String] = Option("eyJhIjoiYiJ9")): JsonData = {
    val data = JsonData(UUID.randomUUID().toString, left, right)

    val insertOrUpdateSQL: SimpleSql[Row] = anorm.SQL(
      s"""
         |INSERT INTO JsonData (${jsonDataObj.fieldsAsString})
         |VALUES ${jsonDataObj.fieldsForValueTypeAsString}
           """.stripMargin
    ).on(jsonDataObj.getParametersForObject(data): _*)
    mySQL.executeUpdate(insertOrUpdateSQL)

    data
  }
}
