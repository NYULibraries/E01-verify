package e01.actors

import org.joda.time._
import org.joda.time.format.ISODateTimeFormat
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import e01.Protocol._

trait SysUtils {
	
	def now(): String = {
    val dt = new DateTime()
    val fmt = ISODateTimeFormat.dateTime()
    fmt.print(dt)
  }

  def createResult(request: Request): String = {
		val json = ( 
			("version" -> request.version) ~ 
			("request_id" -> request.request_id.toString()) ~ 
			("outcome" -> request.outcome.getOrElse(null))  ~ 
			("start_time" -> request.start_time) ~ 
			("end_time" -> request.end_time.getOrElse(null)) ~
			("agent" -> (
				("name" -> request.agent.agent) ~ 
				("version" -> request.agent.version) ~ 
				("host" -> request.agent.host))) ~
			("data" -> request.data.getOrElse(null)) 
		)

		compact(render(json))

	} 

	def getAgent(): Agent = {
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

		new Agent(agent, version, "localhost")
	}
}