package com.adthan.firmakka

import com.adthan.firmakka.messages._


case class MotorPins(vel: Int, dir: Int, current: Int = -1)

class Motor(var velPin: Int, var dirPin: Int, var currentPin: Int = -1) extends ArduinoComponentActor {
  def this(pins: MotorPins) = this(pins.vel, pins.dir, pins.current)

  override def init() {
    arduino ! PinMode(dirPin, OUTPUT)
    arduino ! PinMode(velPin, PWM)
    //if (currentPin !== -1) arduino ! PinMode()
  }

  override def opened() = {
    case MotorForward(vel: Int) =>
      log.info(s"forward $velPin,$vel")
      arduino ! DigitalWrite(dirPin, HIGH)
      arduino ! AnalogWrite(velPin, vel);
    case MotorReverse(vel: Int) =>
      log.info(s"reverse $velPin, $vel")
      arduino ! DigitalWrite(dirPin, LOW)
      arduino ! AnalogWrite(velPin, vel);
    case MotorStop =>
      log.info(s"stop")
      arduino ! DigitalWrite(dirPin, HIGH)
      arduino ! AnalogWrite(velPin, 0)
  }
}