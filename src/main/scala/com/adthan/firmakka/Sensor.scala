
package com.adthan.firmakka

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import akka.actor._
import com.adthan.firmakka.messages._

class Sensor(var pin: Int, interval: FiniteDuration) extends Actor with ArduinoComponentActor with ActorLogging {
  def this(pin: Int) = {
    this(pin, Duration.Zero)
    log.info(s"SensorConstruct $pin")
  }


  override def init() {
    //log.info(s"SensorInit  $pin")
    arduino ! PinMode(pin, ANALOG)
    if (interval > Duration.Zero) {
      context.system.scheduler.schedule(0.seconds, interval) {
        arduino ! AnalogRead(pin)
      }
    }
  }

  override def opened() = {
    case SensorRead =>
      //log.info(s"SensorRead $pin")
      arduino ! AnalogRead(pin)
    case SensorReaded(receivePin, value) =>
      //log.info(s"Sensor $pin, $value")
      context.parent ! SensorReaded(receivePin, value)
  }
}