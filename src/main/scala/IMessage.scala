package test

import play.api.libs.json.{Format, JsError, JsObject, Json, Reads, Writes}
import test.messages.ToolsUpdatedMessage

import scala.reflect.classTag

trait Routable {
  def routingKey: String
}


trait IMessage {
  self =>

  def messageType: String = self.getClass.getSimpleName
}

trait MessageFormatters {
  import test.messages.ToolsUpdatedMessageFormatters._

  implicit val toolsUpdatedMessageFmt = Json.format[ToolsUpdatedMessage]
}

object IMessage extends MessageFormatters {
  private[this] val typePath = "messageType"

  private[this] val toolsUpdatedMessageName =
    classTag[ToolsUpdatedMessage].runtimeClass.getSimpleName

  implicit val fmt: Format[IMessage] = Format(
    Reads(json =>
      (json \ typePath).validate[String].flatMap {
        case `toolsUpdatedMessageName` => json.validate[ToolsUpdatedMessage]

        case unknown => JsError(s"No such message type $unknown")
      }),
    Writes { m: IMessage =>
      val base = m match {
        case m: ToolsUpdatedMessage => toolsUpdatedMessageFmt.writes(m)
      }
      base match {
        case o: JsObject => o ++ Json.obj(typePath -> m.messageType)
        case other       => other
      }
    })
}
