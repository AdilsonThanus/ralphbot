package com.adthan.firmakka

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import scala.concurrent.duration._
import com.adthan.firmakka.messages._

class MotorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with ShouldMatchers
  with FlatSpec
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("ArduinoActorSpec"))

  override def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination(10.seconds)
  }

  "An ArduinoActor" should "be able blink turn on and off a led on arduino board" in {
    val arduino = TestActorRef(Props[ArduinoActor])
    arduino ! Open
    arduino ! PinMode(13, OUTPUT)
    arduino ! AnalogWrite(13, 1)
    arduino ! AnalogWrite(13, 0)
  }

}