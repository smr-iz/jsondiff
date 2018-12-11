package controllers

import common.concurrency.{CommonError, CommonErrorType, ExecutionContexts, JController}
import io.swagger.annotations._
import javax.inject.{Inject, Singleton}
import models._
import play.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, Request}
import services.JsonService

import scala.concurrent.Future

/**
  * Created by semir on 8.12.2018.
  */
@Singleton
class JsonController @Inject()(JsonService: JsonService,
                               executionContexts: ExecutionContexts) extends JController {

  @ApiOperation(
    value = "Setting left side of the data",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(required = true, value = "associated id", dataType = "string", paramType = "query"),
    new ApiImplicitParam(required = true, value = "Json Base 64 encoded", dataType = "string", paramType = "body")
  ))
  def setLeft (
                @ApiParam(value = "Id associated with data") id: String
              ): Action[AnyContent] = Action.async { implicit request =>
    handleExceptions {
      val errorList: CommonError = CommonError()
      val base64EncodedDataAsOpt: Option[String] = request.body.asText

      if (base64EncodedDataAsOpt.isEmpty || base64EncodedDataAsOpt.get.isEmpty) {
        errorList.addError(CommonErrorType.MissingParameter, "encodedData")
        Future.successful(sendJsonError(errorList))
      } else {
        val saveJsResult: CommonError = JsonService.saveJsData(id, JsonType.Left, base64EncodedDataAsOpt.get)

        if (saveJsResult.errorFound) {
          Future.successful(sendJsonError(saveJsResult))
        } else {
          Future.successful(sendJsonOk())
        }
      }
    }
  }

  @ApiOperation(
    value = "Setting right side of the data",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(required = true, value = "associated id", dataType = "string", paramType = "query"),
    new ApiImplicitParam(required = true, value = "Json Base 64 encoded", dataType = "string", paramType = "body")
  ))
  def setRight (
                 @ApiParam(value = "Id associated with data") id: String
               ): Action[AnyContent] = Action.async { implicit request =>
    handleExceptions {
      val errorList: CommonError = CommonError()
      val base64EncodedDataAsOpt: Option[String] = request.body.asText

      if (base64EncodedDataAsOpt.isEmpty) {
        errorList.addError(CommonErrorType.MissingParameter, "encodedData")
        Future.successful(sendJsonError(errorList))
      } else {
        val saveJsResult: CommonError = JsonService.saveJsData(id, JsonType.Right, base64EncodedDataAsOpt.get)

        if (saveJsResult.errorFound) {
          Future.successful(sendJsonError(saveJsResult))
        } else {
          Future.successful(sendJsonOk())
        }
      }
    }
  }

  @ApiOperation(
    value = "Getting left  and right data state",
    httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(required = true, value = "requested id", dataType = "string", paramType = "query")
  ))
  def get (
            @ApiParam(value = "Id associated with data") id: String
          ): Action[AnyContent] = Action.async { implicit request =>
    handleExceptions {
      val commonError: CommonError = CommonError()
      val jsDataResult: Either[CommonError, Option[JsonData]] = JsonService.getJsData(id)

      if (jsDataResult.isLeft) {
        Future.successful(sendJsonError(jsDataResult.left.get))
      } else {
        val jsDataAsOpt: Option[JsonData] = jsDataResult.right.get

        if (jsDataAsOpt.isEmpty) {
          commonError.addError(CommonErrorType.NotFound, "notFoundId")
          Future.successful(sendJsonError(commonError))
        } else {
          val jsData: JsonData = jsDataAsOpt.get
          val result: JsonResult = JsonService.getResult(jsData.left, jsData.right)
          val finalResultDataJs: JsObject = result match {
            case JsonResult.Equal => Json.obj("data" -> jsData.left)
            case JsonResult.DifferentLength => Json.obj("differentLength" -> true)
            case JsonResult.MissingPart => Json.obj("emptyPart" -> true)
            case JsonResult.Different => Json.obj("diff" -> result.diff.sortBy(_.offset))
          }

          Future.successful(sendJsonOk(finalResultDataJs))
        }

      }
    }
  }


  def handleExceptions[R <: Any](block: => Future[play.api.mvc.Result])(implicit request: Request[AnyContent]) = {
    try {
      block
    } catch {
      case e: IllegalArgumentException => Future(BadRequest(e.getMessage))(executionContexts.genericOps)
      case e: Exception => Future(handleUnknownException(e))(executionContexts.genericOps)
    }
  }

  def handleUnknownException(e: Exception)(implicit request: Request[AnyContent]): play.api.mvc.Result = {
    e match {
      case _ => {
        Logger.error("Unhandled Exception", e)
        InternalServerError("Unknown Error")
      }
    }
  }
}
