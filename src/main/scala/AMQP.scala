package e01

import com.typesafe.config._
import com.rabbitmq.client._
import java.util.UUID
import java.io.File
import e01.Protocol._

trait AMQPSupport {

  val conf = ConfigFactory.load() 
  val factory = new ConnectionFactory
  val EXCHANGE_NAME = conf.getString("rabbitmq.exchange")
  
  def getConnection(): Option[Connection] = {
    try {
      factory.setHost(conf.getString("rabbitmq.host"))
      Some(factory.newConnection())
    } catch {
      case e: Exception => None //log error
    }
  }

  def getAMQPConnections(connection: Connection): Option[AMQPConnections] = {
    try {
      val pubChannel = getChannel(connection)
      val cons = getConsumer(getChannel(connection))
      Some(new AMQPConnections(cons, pubChannel))
    } catch {
      case e: Exception => None //log error
    }
  }

  private def getChannel(connection: Connection): Channel = {
    val channel = connection.createChannel()
    channel.exchangeDeclare(EXCHANGE_NAME, "topic")
    val QUEUE_NAME = channel.queueDeclare().getQueue()
    channel
  }

  private def getConsumer(channel: Channel): QueueingConsumer = {
    val QUEUE_NAME = channel.queueDeclare().getQueue()
    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, conf.getString("rabbitmq.consume_key"))
    val consumer = new QueueingConsumer(channel)
    channel.basicConsume(QUEUE_NAME, true, consumer)
    consumer
  }

}
