package test.unmarshallers

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json
import test.{IMessage, RemoteMessage}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

case class RabbitMessage(key: String, data: String)

class RabbitMessageUnmarshaller(implicit executionContext: ExecutionContext)
    extends MessageUnmarshaller[RabbitMessage, RemoteMessage]{

  override def handle(rabbitMessage: RabbitMessage): Option[RemoteMessage] = {
    Try {
      println(rabbitMessage.data)
      val json = Json.parse(rabbitMessage.data)

      val data = IMessage.fmt.reads(json).get

      (DateTime.now(DateTimeZone.UTC), data)
    } match {
      case Success((timestamp: DateTime, message: IMessage)) =>
        Some(RemoteMessage(message, timestamp))

      case Failure(exception) =>
        println(exception)
        None
    }
  }
}
