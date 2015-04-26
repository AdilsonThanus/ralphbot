package com.adthan.firmakka

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import com.adthan.firmakka.messages.{SensorReaded, PulseInRead, PulseIn, HIGH}
import scala.concurrent.duration._

class Ping(pin: Int, freq: Int, pulse: FiniteDuration ) extends Actor with ArduinoComponentActor with ActorLogging {
  def this(pin:Int) = this(pin,100,500.millis)
  var last, median = 0
  var samples = scala.collection.SortedSet[Int](0)
  var microseconds = 0

  //var settings = new PulseInSettings(pin,HIGH.value,5)

  //    this.io.setMaxListeners(100);
  // Interval for polling pulse duration
  override def init() {
    context.system.scheduler.schedule(0.seconds, pulse) {
      arduino ! PulseIn(pin, HIGH, 5)
    }

    context.system.scheduler.schedule(pulse, pulse) {
      arduino ! PulseInRead(pin)
      //var err = null
      var medianIndex = Math.floor(samples.size / 2).toInt
      var arr = samples.toArray
      if (arr.size >0) {
        median = arr(medianIndex)
      }
      //if (median != last ) median = last

      //log.info(s"median $median, last $last")

      // If the median value for this interval is not the same as the
      // median value in the last interval, fire a "change" event.
      if (median < last - 32 || median > last + 32) {
        context.parent ! SensorReaded(pin, median)
      }
      last = median
      samples = scala.collection.SortedSet[Int]()

    }
  }

  def opened() = {
    case SensorReaded(`pin`, value) =>
      microseconds = value
      samples += value
  }

}
