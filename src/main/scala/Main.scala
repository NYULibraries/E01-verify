package e01

import akka.actor.{ ActorSystem, Props }
import java.io.File
import e01.actors._
import Protocol._

object Main extends App {

	// initialize the actor system and log
	val system = ActorSystem("E01_Validator")	


	//initialize supervisor
	val supervisor = system.actorOf(Props[Supervisor], "supervisor")
	
	//verify an E01 image
	//confirm the file is an e01 with ewfinfo
  
	//verify the e01 with ewf verify
	//supervisor ! new Verify(e01, log)
	
}