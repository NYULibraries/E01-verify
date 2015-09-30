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
  case class VerifyComplete(id: UUID, filename: String)
  case class WriteLog(message: String)
  case class AMQPConnections(consumer: QueueingConsumer, publisher: Channel)
  
  case class Event(
  	requestId: UUID, 
  	eventType: String,
  	path: String, 
  	start: String, 
  	end: Option[String], 
  	result: Option[String]
  )
  
  case class Agent(
  	name: String,
  	version: String,
  	host: String
  )

  case class Response(
  	version: String,
  	request_id: String,
  	outcome: String,
  	start_time: String,
  	end_time: String,
  	agent: Agent,
  	data: Option[String]
  )

}