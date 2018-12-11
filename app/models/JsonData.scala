package models

import java.sql.Timestamp

import anorm.{NamedParameter, Row, SimpleSql}
import common.concurrency.{CommonError, CommonErrorType}
import javax.inject.{Inject, Singleton}
import models.mysql.MySQL
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{Json, OWrites}

/**
  * Created by semir on 8.12.2018.
  */
case class JsonData (id: String,
                     var left: Option[String],
                     var right: Option[String],
                     var uDate: DateTime = DateTime.now(),
                     cDate: DateTime = DateTime.now())

@Singleton
class JsonDataObj @Inject()(mySQL: MySQL) {
  val fieldsAsString = "`id`, `left`, `right`, `uDate`, `cDate`"
  val fieldsForValueTypeAsString = "({id}, {left}, {right}, {uDate}, {cDate})"

  def getParametersForObject(obj: JsonData): Seq[NamedParameter] = {
    var parameters = Seq.empty[NamedParameter]

    parameters +:= ("id" -> obj.id: NamedParameter)
    parameters +:= ("left" -> obj.left: NamedParameter)
    parameters +:= ("right" -> obj.right: NamedParameter)
    parameters +:= ("uDate" -> new Timestamp(obj.uDate.getMillis): NamedParameter)
    parameters +:= ("cDate" -> new Timestamp(obj.cDate.getMillis): NamedParameter)

    parameters
  }

  def convertFromRow(row: Row): JsonData = {
    JsonData(
      row[String]("JsonData.id"),
      row[Option[String]]("JsonData.left"),
      row[Option[String]]("JsonData.right"),
      row[DateTime]("JsonData.udate"),
      row[DateTime]("JsonData.cDate")
    )
  }

  def insertOrUpdate(jsonData: JsonData): CommonError = {
    val commonError: CommonError = CommonError()

    try {
      val insertOrUpdateSQL: SimpleSql[Row] = anorm.SQL(
        s"""
           |INSERT INTO JsonData ($fieldsAsString)
           |VALUES $fieldsForValueTypeAsString
           |ON DUPLICATE KEY UPDATE `left` = {left}, `right` = {right}
           """.stripMargin
      ).on(getParametersForObject(jsonData): _*)

      mySQL.executeUpdate(insertOrUpdateSQL)
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to insert or update json data with id: ${jsonData.id} with error: $e")
        commonError.addError(CommonErrorType.DB, "dbInsertException")
    }

    commonError
  }

  def getJsDataById(id: String): Either[CommonError, Option[JsonData]] = {
    val commonError: CommonError = CommonError()

    try {
      val selectSql = anorm.SQL(
        s"""
           |SELECT *
           |  FROM JsonData
           | WHERE id = {id}
         """.stripMargin).on("id" -> id)

      Right(mySQL.apply(selectSql).map(row => convertFromRow(row)).headOption)
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to get js data from db with id: $id with error: $e")
        commonError.addError(CommonErrorType.DB, "dbGetException")
        Left(commonError)
    }
  }
}

sealed trait JsonType
object JsonType extends Enumeration {
  case object Left extends JsonType
  case object Right extends JsonType
}

sealed trait JsonResult {var diff: List[Diff] = List.empty}
object JsonResult extends Enumeration {
  case object Equal extends JsonResult
  case object DifferentLength extends JsonResult
  case object Different extends JsonResult
  case object MissingPart extends JsonResult
}

case class Diff(offset: Int, length: Int)
object Diff {
  implicit val write: OWrites[Diff] = Json.writes[Diff]

}
