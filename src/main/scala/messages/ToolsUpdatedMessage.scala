package test.messages

import play.api.libs.json.Json
import test.{IMessage, Routable}

case class UpdatedToolList(updatedTools: List[UpdatedTool]) extends IMessage

case class UpdatedTool(uuid: String, version: String, dockerImageVersion: String)

case class ToolsUpdatedMessage(tools: UpdatedToolList) extends IMessage with Routable {
  override def routingKey: String = ToolsUpdatedMessage.routingKey
}

object ToolsUpdatedMessage {
  val routingKey = "system.tools.updated"
}

object ToolsUpdatedMessageFormatters {
  implicit val updatedToolFmt = Json.format[UpdatedTool]
  implicit val updatedToolListsFmt = Json.format[UpdatedToolList]

  implicit val ToolsUpdatedMessageFmt = Json.format[ToolsUpdatedMessage]
}
