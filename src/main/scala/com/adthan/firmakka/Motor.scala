package com.adthan.firmakka

import com.adthan.firmakka.messages._

class Motor(pwd: Int, dir: Int) extends ArduinoComponentActor {


  override def init() {
    arduino ! PinMode(dir, OUTPUT)
    arduino ! PinMode(pwd, PWM)
  }

  override def opened() = {
    case MotorForward(vel: Int) =>
      arduino ! DigitalWrite(dir, HIGH)
      arduino ! AnalogWrite(pwd, vel);
    case MotorReverse(vel: Int) =>
      arduino ! DigitalWrite(dir, LOW)
      arduino ! AnalogWrite(pwd, vel);
    case MotorStop =>
      arduino ! DigitalWrite(dir, HIGH)
      arduino ! AnalogWrite(pwd, 0)
  }
}