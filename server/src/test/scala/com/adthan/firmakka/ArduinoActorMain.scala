package com.adthan.firmakka

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.adthan.firmakka.messages._
import scala.concurrent.duration._

object ArduinoActorMain extends App {
  val system = ActorSystem("arduinoakka")
  /*
    //  val arduino = system.actorOf(Props(classOf[ArduinoActor], "/dev/ttyACAM0", 56700), "arduino")
    val arduino = system.actorOf(Props(classOf[ArduinoActor]), "arduino")

    arduino ! Open

    Thread.sleep(4600)
    arduino ! PinMode(3, ANALOG)
    Thread.sleep(500)
    arduino ! AnalogRead(3)
    Thread.sleep(500)
    arduino ! AnalogRead(3)
    Thread.sleep(500)
    arduino ! PinMode(2, SERVO)
    Thread.sleep(200)
    arduino ! PinMode(4, SERVO)
    Thread.sleep(200)
    arduino ! PinMode(6, SERVO)
    Thread.sleep(200)
    arduino ! ServoWrite(2, 100)
    Thread.sleep(200)
    arduino ! ServoWrite(2, 130)

  */
  //  val sensor = system.actorOf(Props(classOf[Sensor],3), "sensor")
  val arduino = system.actorOf(Props(classOf[ArduinoActor]), "arduino")
  arduino ! Open
  Thread.sleep(4600)
  //val sensor = system.actorOf(Props(classOf[Sensor], 3, 1500.milliseconds), "sensorIR")
  val sensor = system.actorOf(Props(classOf[Sensor], 3), "sensorIR")
  Thread.sleep(600)
  sensor ! SensorRead
  Thread.sleep(500)
  sensor ! SensorRead
  Thread.sleep(600)
  sensor ! SensorRead
  Thread.sleep(500)
  sensor ! SensorRead
  Thread.sleep(600)
  sensor ! SensorRead
  Thread.sleep(500)
  sensor ! SensorRead
  Thread.sleep(600)
  sensor ! SensorRead
  Thread.sleep(500)
  sensor ! SensorRead
 // sensor ! SensorRead
 // sensor ! SensorRead
 // sensor ! SensorRead
  //
  //  //system.scheduler.schedule(0 milli, 500 milli, {arduino ! AnalogWrite(13, 1) })
  //
  //
  //  arduino ! DigitalWrite(13, 0)

  //  arduino ! DigitalWrite(13, 1)
  //  Thread.sleep(500)
  //
  //  arduino ! DigitalWrite(13, 0)
  //  Thread.sleep(500)
  //  arduino ! Close

  system.shutdown
}
