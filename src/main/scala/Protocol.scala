package e01

import java.io.File
import java.util.UUID
import com.rabbitmq.client.{ QueueingConsumer, Channel }

object Protocol {
  
  case object VerifySuccess
  case object VerifyFailure
  case object PrintLog
  case object MessageError
  case object GetResult
  case object Listen

  case class VerifyResult(id: UUID)
  case class VerifyRequest(id: UUID, file: File)
  case class VerifyComplete(id: UUID)
  case class WriteLog(message: String)
  case class AMQPConnections(consumer: QueueingConsumer, publisher: Channel)
  case class Publish(message: String)

  
  case class Agent(
  	agent: String,
  	version: String,
  	host: String
  )

  case class Request(
  	version: String,
  	request_id: UUID,
    file: File,
  	outcome: Option[String],
  	start_time: String,
  	end_time: Option[String],
  	agent: Agent,
  	data: Option[String]
  )

}