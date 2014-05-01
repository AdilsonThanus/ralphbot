package com.adthan.firmakka

import akka.actor._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import com.adthan.firmakka.messages.{Opened, Open}

//import akka.dispatch.ExecutionContexts


trait ArduinoComponentActor extends Actor with ActorLogging {
  import context.system
  var arduino: ActorRef = null

  var future = system.actorSelection("/user/arduino").resolveOne(5.seconds)
  future.onSuccess({
    case arduino: ActorRef =>
      log.info("Found arduino actor")
      openArduino(arduino)
  })
  future.onFailure({
    case _ =>
      //log.error()
      log.info("Arduino actor not foud. Creating one ...")
      openArduino(system.actorOf(Props(classOf[ArduinoActor]), "arduino"))
  })

  def openArduino(arduinoRef: ActorRef) {
    arduino = arduinoRef
    arduino ! Open
  }

  override def receive = {
    case Opened =>
      init()
      context.become(opened())
      context.parent ! Opened
    //    case _ =>
    //     log.info("Arduino component not initialized")
    //    sender ! NotInitialized
  }

  def opened(): Receive

  def init()
}
