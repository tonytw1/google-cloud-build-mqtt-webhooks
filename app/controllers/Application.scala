package controllers

import java.util.Base64

import model.{Attributes, Message, Push}
import mqtt.MQTTService
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BodyParsers, Controller}

import scala.concurrent.Future

object Application extends Controller {

  val mqttService = MQTTService

  def home: Action[AnyContent] = Action.async {
    Future.successful(Ok(Json.toJson("Ok")))
  }

  def webhook: Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
    val message = request.body
    val authorizationHeader = request.headers.get("Authorization")
    Logger.info("Received webhook: " + message + " with Authorization header: " + authorizationHeader)

    implicit val ar = Json.reads[Attributes]
    implicit val mr = Json.reads[Message]
    implicit val pr = Json.reads[Push]

    val push = message.as[Push]
    val data = push.message.data
    Logger.info("Message data: " + data)

    val dataJson = Base64.getDecoder.decode(data).toString  // TODO utf8
    Logger.info("Decoded data: " + dataJson)

    mqttService.publish(dataJson)

    Future.successful(Ok(Json.toJson("Thanks!")))
  }

}