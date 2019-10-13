package controllers

import mqtt.MQTTService
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, BodyParsers, Controller}

import scala.concurrent.Future

object Application extends Controller {

  val mqttService = MQTTService

  def home = Action.async { request =>
    Future.successful(Ok(Json.toJson("Ok")))
  }

  def webhook = Action.async(BodyParsers.parse.json) { request =>
    val message = request.body
    val authorizationHeader = request.headers.get("Authorization")
    Logger.info("Received webhook: " + message + " with Authorization header: " + authorizationHeader)
    mqttService.publish(message.toString())
    Future.successful(Ok(Json.toJson("Thanks!")))
  }

}