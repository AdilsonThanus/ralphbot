package com.adthan.firmakka

import akka.actor._
import com.adthan.firmakka.messages._


class Servo(var pin: Int) extends ArduinoComponentActor with ActorLogging {

  val CENTER = 90
  var step = 5
  var pos = CENTER

  override def init() {
    arduino ! PinMode(pin, SERVO)
    arduino ! ServoWrite(pin, pos)
  }

  override def opened() = {
    case ServoMove(value: Int) =>
      pos = value
      move()
    case ServoSetStep(newStep: Int) => step = newStep
    case ServoInc =>
      pos += step
      move()
    case ServoDec =>
      pos -= step
      move()
  }

  def move() {
    if (pos > 180) pos = 180
    if (pos < 0) pos = 0
    arduino ! ServoWrite(pin, pos)
    sender ! ServoMoved(pin, pos)
    //log.debug(s"Servo move -  pin: $pin, pos: $pos")
  }
}