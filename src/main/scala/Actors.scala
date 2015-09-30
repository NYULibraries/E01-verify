package e01.actors

import akka.actor.{ Actor, ActorRef, Props, PoisonPill }
import akka.pattern.ask
import akka.util.Timeout
import java.io.{ File, FileWriter }
import java.util.UUID
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import com.rabbitmq.client.Connection
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.io.Source
import scala.language.postfixOps

import e01.Protocol._
import e01.{ AMQPSupport, EventBuilder }


class Supervisor() extends Actor with EventBuilder {

	implicit val timeout = new Timeout(5 seconds)

	val validatorProps = Props(new Validator())
	val validator = context.actorOf(validatorProps, "Validator")

	val logReaderProps = Props(new LogReader())
	val logReader = context.actorOf(logReaderProps, "Log_Reader")

	val consumerProps = Props(new Consumer(self))
	val consumer = context.actorOf(consumerProps, "Consumer")
	
	getVersion

	consumer ! Listen

  var events = Map.empty[UUID, Event]

	def receive = {
		
		case vr: VerifyRequest => {
				println("* Request to verify " + vr.file.getName + " received" )
				events = events + (vr.id -> newEvent(vr.id, "VERIFY", vr.file.getAbsolutePath))
				println(events(vr.id))
				validator ! vr
		}

		case vc: VerifyComplete => { 

			//check the validation result
			val future = logReader ? new VerifyResult(vc.id)    
    	val result = Await.result(future, timeout.duration).asInstanceOf[Boolean]
    	
    	result match {
    		case true => println("* verification result for " + vc.filename + ": SUCCESS")
    		case false => println("* verification result for " + vc.filename + ": FAILURE")
    	}
	  }
		
		case _ => println("UNKNOWN MESSAGE")
	}

	def getVersion() {
		import scala.sys.process._
		val command = Seq("ewfinfo", "-V")
		var version = "NO_VERSION"
		var agent = "NO_AGENT"

		val ewfVersion = "^ewfinfo.*".r
		
		val logger = ProcessLogger( 
			(o: String) => { 
				o match {
					case ewfVersion(_*) => { 
						agent = o.split(" ")(0)
						version = o.split(" ")(1)
					}
					case _ =>
				} 
			})
		
		command ! logger

		val command2 = ()

		println("agent " + agent)
		println("version " + version)
	}
}

class Validator() extends Actor {
	import scala.sys.process._

	def receive = {
		case v: VerifyRequest => {

			val log = new File("logs/" + v.id.toString + ".log")
			log.createNewFile
			val writerProps = Props(new LogWriter(log))
			val logWriter = context.actorOf(writerProps, "Log_Writer")
			
			val command = Seq("ewfverify", v.file.getAbsolutePath())
			
			val logger = ProcessLogger(
				(o: String) => logWriter ! (o + "\n"), 
				(e: String) => logWriter ! (e + "\n"))
			
			command ! logger


			logWriter ! PoisonPill
			sender ! VerifyComplete(v.id, v.file.getName)
		}

		case _ => println("UNKNOWN MESSAGE")
	}
}

class LogWriter(log: File) extends Actor {
	val writer = new FileWriter(log)
	
	override def postStop() {
  	writer.close
	}

	def receive = {
		
		case message: String => {
			if(!message.equals("\n")) {
			  writer.append(message)
			  writer.flush
			}
		}

		case _ => println("UNKNOWN MESSAGE")
	}
}

class LogReader() extends Actor {

	val success = "^ewfverify: SUCCESS".r
	val failure = "^ewfverify: FAILURE".r 
	
	def receive = {

		case v: VerifyResult => {

			val log = new File("logs/" + v.id.toString + ".log")		
			val lines = Source.fromFile(log).getLines

			lines.foreach { line =>
				line match {
					case success(_*) => sender ! true
					case failure(_*) => sender ! false
					case _ =>
				}
			}
		}

		case _ => println("UNKNOWN MESSAGE")
	}
}

class Consumer(supervisor: ActorRef) extends Actor with AMQPSupport {
	val connection = getConnection.get
  val connections = getAMQPConnections(connection).get
  implicit val formats = DefaultFormats

  def receive = {
  	case Listen => {
  		val delivery = connections.consumer.nextDelivery()
      val message = new String(delivery.getBody())
      val json = parse(message)
      val request_id = (json \ "request_id").extract[String]
      val request_path = (json \ "path").extract[String]
      val request_type = (json \ "type").extract[String]

      request_type match {

      	case "VERIFY" => { 
      		val request = new VerifyRequest(UUID.fromString(request_id), new File(request_path))
      		supervisor ! request
      	}
      }
      //do something with a message
      self ! Listen 
  	}
  	case _ => println("Hi")
  }
}