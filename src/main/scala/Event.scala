package e01

import org.joda.time._
import org.joda.time.format.ISODateTimeFormat
import java.util.UUID
import Protocol._

trait EventBuilder {
  
  def newEvent(requestId: UUID, eventType: String, path: String): Event = {
    new Event(requestId, eventType, path, now, None, None)
  }

  def now(): String = {
    val dt = new DateTime()
    val fmt = ISODateTimeFormat.dateTime()
    fmt.print(dt)
  }

  def endEvent(event: Event) : Event = event.copy(end = Some(now)) 
  def addResult(event: Event, eventResult: String): Event = { event.copy(result = Some(eventResult)) }

}
