package e01

import akka.actor.{ ActorSystem, Props }
import java.io.File
import e01.actors._
import Protocol._

object Main extends App with TestRequestSupport {

	// initialize the actor system and log
	val system = ActorSystem("E01_Validator")	

	//initialize supervisor
	val supervisor = system.actorOf(Props[Supervisor], "supervisor")
	
	//run a test
	testRequest()
}