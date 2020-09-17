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
    val decodedData = Base64.getDecoder.decode(data)
    val decodedDataJson = Json.parse(decodedData)
    val jsonMessageString = Json.prettyPrint(decodedDataJson)
    Logger.info("Decoded message data: " + jsonMessageString)

    val status = push.message.attributes.status
    Logger.info("Message status: " + status)

    mqttService.publish(jsonMessageString, status)
    Future.successful(Ok(Json.toJson("Thanks!")))
  }

}