package com.adthan.firmakka

import com.adthan.firmakka.messages._


case class MotorPins(vel: Int, dir: Int, current: Int = -1)

class Motor(var velPin: Int, var dirPin: Int, var curtPin: Int = -1) extends ArduinoComponentActor {
  def this(pins: MotorPins) = this(pins.vel, pins.dir)

  override def init() {
    arduino ! PinMode(dirPin, OUTPUT)
    arduino ! PinMode(velPin, PWM)
  }

  override def opened() = {
    case MotorForward(vel: Int) =>
      arduino ! DigitalWrite(dirPin, HIGH)
      arduino ! AnalogWrite(velPin, vel);
    case MotorReverse(vel: Int) =>
      arduino ! DigitalWrite(dirPin, LOW)
      arduino ! AnalogWrite(velPin, vel);
    case MotorStop =>
      arduino ! DigitalWrite(dirPin, HIGH)
      arduino ! AnalogWrite(velPin, 0)
  }
}