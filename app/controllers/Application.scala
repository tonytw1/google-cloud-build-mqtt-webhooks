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

  var publishedTimeWaterMark = ""

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

    val messagePublishTime = push.message.publishTime
    Logger.info("Received webhooked message with publishTime: " + messagePublishTime)
    if (messagePublishTime > publishedTimeWaterMark) {
      publishedTimeWaterMark = messagePublishTime
      Logger.info("Decoded message data: " + jsonMessageString)

      val status = push.message.attributes.status
      Logger.info("Message status: " + status)
      mqttService.publish(jsonMessageString, status)

    } else {
      // TODO This consumer concern should be pushed down to the clients who care about it; we just need to capture the webhooks without opinion.
      Logger.warn("Ignoring out of order message: " + messagePublishTime + " / " + publishedTimeWaterMark)
    }

    Future.successful(Ok(Json.toJson("Thanks!")))
  }

}