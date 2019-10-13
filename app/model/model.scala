package model

case class Push(message: Message, subscription: String)

case class Message(messageId: String, publishTime: String, attributes: Attributes, data: String)
case class Attributes(buildId: String, status: String)
