package controllers

import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{BodyParsers, Action, Controller}

import scala.concurrent.Future

object Application extends Controller {

  def webhook = Action.async(BodyParsers.parse.json) { request =>
    val message: JsValue = request.body
    Logger.info("Received webhook: " + message)
    Future.successful(Ok(Json.toJson("Thanks!")))
  }

}