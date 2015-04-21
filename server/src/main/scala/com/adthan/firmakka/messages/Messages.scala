package com.adthan.firmakka.messages

// Messages
case object NotInitialized

case object Open

case object Opened

case class FailToOpen(error: String)

case object Close

case object Closed

case class AnalogRead(pin: Int)

case class AnalogWrite(pin: Int, value: Int)

case class DigitalRead(pin: Int)

case class DigitalWrite(pin: Int, value: DigitalValue)

case class ServoWrite(pin: Int, value: Int)

case class PinMode(pin: Int, pinMode: Mode)

//Sensor
case object SensorRead

//Ping
case class PulseIn(pin: Int, value: DigitalValue, pulseOut: Int = 0, timeout: Int = 1000000)

case class PulseInRead(pin: Int)

//Servo
case class ServoSetStep(step: Int)

case class ServoMove(value: Int)

case object ServoInc

case object ServoDec

case class ServoMoved(pin: Int, value: Int)

//Motor
case object MotorStop

case class MotorForward(vel: Int)

case class MotorReverse(vel: Int)

//Pin modes
sealed trait Mode {
  def mode: Int
}

case object INPUT extends Mode {
  val mode = 0
}

case object OUTPUT extends Mode {
  val mode = 1
}

case object ANALOG extends Mode {
  val mode = 2
}

case object
PWM extends Mode {
  val mode = 3
}

case object SERVO extends Mode {
  val mode = 4
}

case object SHIFT extends Mode {
  val mode = 5
}

case object I2C extends Mode {
  val mode = 6
}

//Pin digital values
sealed trait DigitalValue {
  def value: Int
}

case object LOW extends DigitalValue {
  val value = 0
}

case object HIGH extends DigitalValue {
  val value = 1
}

case class SensorReaded(pin: Int, value: Int)
