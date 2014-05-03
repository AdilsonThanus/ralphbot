package com.adthan.firmakka

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import scala.concurrent.duration._
import com.adthan.firmakka.messages._


//class SensorSpec
//  extends TestKit(ActorSystem("testSystem"))
//  with ImplicitSender
//  with ShouldMatchers
//  with FlatSpec
//  with BeforeAndAfterAll {
//
//  //def this() = this(ActorSystem("ArduinoActorSpec"))
//
//  override def beforeAll() {
//    // val arduino = system.actorOf(Props(classOf[ArduinoActor]), "arduino")
//    val arduino = TestActorRef(Props(classOf[ArduinoActor]), "arduino")
//    arduino ! Open
//    expectMsg(5.seconds, Opened)
//  }
//
//  override def afterAll(): Unit = {
//    system.shutdown()
//    system.awaitTermination(10.seconds)
//  }
//
//  "An Sensor" should "be return a value readfrom a arduino board" in {
//    //actorOf(Props(classOf[Sensor], 3), "sensorIR")
//    //val sensor = _system.actorOf(Props(classOf[Sensor], 3,5500.milliseconds),"sensor")
//    val sensor = system.actorOf(Props(classOf[Sensor], 3), "sensor")
//    //    ir = context.actorOf(Props(classOf[Sensor], arduino, 3, 30500.milliseconds), "sensorIR")
//    sensor ! SensorRead
//    sensor ! SensorRead
//    sensor ! SensorRead
//    sensor ! SensorRead
//    sensor ! SensorRead
//    sensor ! SensorRead
//
//    within(15.seconds) {
//      var x:Int =0
//      println("1 >>>", expectMsg(5.seconds, SensorReaded(3, x)))
//      println("2 >>>", expectMsg(5.seconds, SensorReaded(3, 0)))
//      println("3 >>>", expectMsg(5.seconds, SensorReaded(3, 0)))
//      println("4 >>>", expectMsg(5.seconds, SensorReaded(3, 0)))
//      // expectMsg(5.seconds,Opened)
//      println(">>>", expectMsg(5.seconds, SensorReaded(3, 0)))
//    }
//  }
//
//}