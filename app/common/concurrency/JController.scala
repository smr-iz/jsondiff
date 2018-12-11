package common.concurrency

import play.api.libs.json.{JsBoolean, JsValue}
import play.api.mvc.{Controller, Result}

/**
  * Created by semir on 9.12.2018.
  */
trait JController extends Controller {
  def sendJsonError(errorData: CommonError): Result = {
    sendJsonResponse(None, Some(errorData.toJson))
  }

  def sendJsonOk(successData: JsValue = JsBoolean(true)): Result = {
    sendJsonResponse(Some(successData), None)
  }

  def sendJsonResponse(successData: Option[JsValue] = None, errorData: Option[JsValue] = None): Result = {
    var jsObject = play.api.libs.json.Json.obj()

    successData.foreach { sd =>
      jsObject += ("success", sd)
    }

    errorData.foreach { ed =>
      jsObject += ("error", ed)
    }

    Ok(jsObject)
  }
}
