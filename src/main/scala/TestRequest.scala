package e01

trait TestRequestSupport {
	import org.json4s._
	import org.json4s.JsonDSL._
	import org.json4s.jackson.JsonMethods._
	import com.rabbitmq.client._
  import java.io.File
	import java.util.UUID
	import com.typesafe.config._

	val conf = ConfigFactory.load() 

	def testRequest() {
		

	  getConnection match {

    	case Some(connection) => {  
  	 		val channel = connection.createChannel()
  	  	val EXCHANGE_NAME = conf.getString("rabbitmq.exchange")
  	  	channel.exchangeDeclare(EXCHANGE_NAME, "topic")
  	  	val QUEUE_NAME = channel.queueDeclare().getQueue()
        val key = conf.getString("rabbitmq.test_request_key")
  	  	channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, key)
  			
  			val json = ( 
          ("version" -> "0.0.1") ~ 
          ("request_id" -> UUID.randomUUID.toString) ~ 
          ("params" -> ("request_path" -> conf.getString("rabbitmq.test_file_loc")))
        ) 	
  			
        channel.basicPublish(EXCHANGE_NAME, key, null, compact(render(json)).getBytes())
  	  	
        channel.close
  	  	connection.close
    	
      }
    
    	case None => System.err.println("Connection to message broker unsuccessfull")
  	}
  }

  def getConnection(): Option[Connection] = {
    try {
      val factory = new ConnectionFactory
      factory.setHost(conf.getString("rabbitmq.host"))
      Some(factory.newConnection())
    } catch {
      case e: Exception => None
    }
  }
}