package common.concurrency

import common.concurrency.CommonErrorType.CommonErrorType
import play.api.libs.json._

import scala.collection.mutable

/**
  * Created by semir on 9.12.2018.
  */
case class CommonError() {
  var errorFound: Boolean = false
  private val defaultError: mutable.Map[String, Error] = mutable.Map.empty[String, Error]

  def addError(key: CommonErrorType, value: String): Unit = {
    addError(key.toString, new SimpleError(value))
  }

  def addError(key: String, error: Error): Unit = {
    errorFound = true
    defaultError.put(key, error)
  }

  def toJson: JsObject = {
    var returnValue: JsObject = Json.obj()

    if (defaultError.size > 0) {
      returnValue += ( "default" -> JsObject(defaultError.map { case (k: String, v: Error) => (k, v.toJson) }.toList) )
    }

    returnValue
  }
}

abstract class Error(value: Any) {
  def toJson: JsValue

  def getValue = value
}

case class SimpleError(value: JsValue) extends Error(value) {
  def this(value: String) = this(JsString(value))

  def this() = this(JsBoolean(value = true))

  override def toJson: JsValue = {
    value
  }

  override def toString = value.as[String]
}

object CommonErrorType extends Enumeration {
  type CommonErrorType = Value
  val MissingParameter,
  NotFound,
  DB = Value
}