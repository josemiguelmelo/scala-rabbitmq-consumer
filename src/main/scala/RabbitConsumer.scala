package test

import akka.actor.{ActorSystem, Props}
import akka.stream.Supervision.Decider
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.rabbitmq.client.Channel
import com.spingo.op_rabbit.Binding.{Concrete, QueueDefinition}
import com.spingo.op_rabbit.stream.RabbitSource
import com.spingo.op_rabbit.{RabbitControl, RecoveryStrategy}
import com.spingo.op_rabbit.Directives._
import com.timcharper.acked.AckedSink
import org.joda.time.DateTime
import test.messages.ToolsUpdatedMessage
import test.unmarshallers.{RabbitMessage, RabbitMessageUnmarshaller}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

case class RemoteMessage(message: IMessage, timestamp: DateTime)

object RabbitConsumer {

  val queueName = "codacy.tools.hear"

  val routes = List(ToolsUpdatedMessage.routingKey)

  implicit val actorSystem = ActorSystem("such-system")
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(actorSystem)
      .withSupervisionStrategy(Supervision.resumingDecider: Decider)
  )

  implicit val ec = ExecutionContext.Implicits.global
  val rabbitControl = actorSystem.actorOf(Props[RabbitControl])

  implicit val recoveryStrategy = RecoveryStrategy.limitedRedeliver(20.seconds)

  implicit val rabbitMessageUnmarshaller = new RabbitMessageUnmarshaller()
  def run() = {
    RabbitSource(
      rabbitControl,
      channel(qos = 3),
      consume(topic( queue(queueName), routes)),
      body(as[String]) & routingKey
    ).runForeach {
      case (msg, key) =>
        val unmarshalledMessage = rabbitMessageUnmarshaller.handle(RabbitMessage(key, msg))

        unmarshalledMessage match {
          case Some(remoteMessage) =>
            println(remoteMessage)
          case _ =>
            println(s"Message: ($msg) with key ($key) could not be processed.")
            throw new Exception(s"Message: ($msg) with key ($key) could not be processed.")
        }
    }
  }
}
