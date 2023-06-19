package model

import play.api.libs.json.{Json, Reads}

case class Push(message: Message, subscription: String)

object Push {
  implicit val pr: Reads[Push] = Json.reads[Push]
}

case class Message(messageId: String, publishTime: String, attributes: Attributes, data: String)

object Message {
  implicit val mr: Reads[Message] = Json.reads[Message]
}

case class Attributes(buildId: String, status: String)

object Attributes {
  implicit val ar: Reads[Attributes] = Json.reads[Attributes]
}
