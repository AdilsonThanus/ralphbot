package com.adthan.firmakka

import org.scalatest.{ BeforeAndAfterAll, FlatSpec }
import org.scalatest.matchers.ShouldMatchers
import akka.actor.{Props, ActorSystem}
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import scala.concurrent.duration._
import com.adthan.firmakka.messages._

class ArduinoActorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with ShouldMatchers
  with FlatSpec
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("ArduinoActorSpec"))

  val arduino = TestActorRef(Props[ArduinoActor])

  override def afterAll(): Unit = {
    arduino ! Close
    system.shutdown()
    system.awaitTermination(30.seconds)
  }

  "An ArduinoActor" should "be able blink turn on and off a led on arduino board" in {
    arduino ! Open
    expectMsg(5.seconds, Opened)
    arduino ! AnalogRead(3)
    expectMsg(SensorReaded(3, 0))
    arduino ! PinMode(13, OUTPUT)
    arduino ! AnalogWrite(13, 1)
    arduino ! AnalogWrite(13, 0)
    arduino ! AnalogRead(3)
    expectMsg(SensorReaded(3, 0))
    arduino ! AnalogRead(3)
    expectMsg(SensorReaded(3, 0))
    arduino ! AnalogRead(3)
    expectMsg(SensorReaded(3, 0))
    arduino ! AnalogRead(3)
    expectMsg(SensorReaded(3, 0))
    arduino ! AnalogRead(3)
    expectMsg(SensorReaded(3, 0))

    arduino ! PinMode(6, SERVO)
    arduino ! ServoWrite(6, 140)
    arduino ! ServoWrite(6, 160)
    arduino ! ServoWrite(6, 120)
  }

}