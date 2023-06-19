package controllers

import model.Push
import mqtt.MQTTService
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import java.util.Base64
import javax.inject.Inject
import scala.concurrent.Future

class Application @Inject()(cc: ControllerComponents, mqttService: MQTTService) extends AbstractController(cc) {

  private val logger: Logger = Logger(this.getClass)

  def home: Action[AnyContent] = Action.async {
    Future.successful(Ok(Json.toJson("Ok")))
  }

  private var publishedTimeWaterMark = ""

  def webhook: Action[JsValue] = Action(parse.json) { request =>
    val message = request.body
    val authorizationHeader = request.headers.get("Authorization")
    logger.info("Received webhook: " + message + " with Authorization header: " + authorizationHeader)

    val push = message.as[Push]
    val data = push.message.data
    val decodedData = Base64.getDecoder.decode(data)
    val decodedDataJson = Json.parse(decodedData)
    val jsonMessageString = Json.prettyPrint(decodedDataJson)

    val messagePublishTime = push.message.publishTime
    logger.info("Received webhooked message with publishTime: " + messagePublishTime)

    publishedTimeWaterMark = messagePublishTime
    logger.info("Decoded message data: " + jsonMessageString)

    val status = push.message.attributes.status
    logger.info("Message status: " + status)
    mqttService.publish(jsonMessageString, status)

    Ok(Json.toJson("Thanks!"))
  }

}