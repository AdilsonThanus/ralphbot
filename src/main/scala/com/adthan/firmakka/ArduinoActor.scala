package com.adthan.firmakka

import akka.actor.Actor
import akka.actor.ActorLogging
import com.adthan.firmakka
import com.adthan.sjf.{Serial, Firmata}
import com.adthan.firmakka.messages._

object ArduinoObj {
  def apply(port: String, baud: Int) = {
    this.port = port
    this.baud = baud
    this
  }

  def apply(port: String) = {
    this.port = port
    this
  }

  var serial: Serial = null
  var firmata: Firmata = new Firmata(new FirmataWriter())
  var port = Serial.list().head
  var baud: Int = 56700

  def open() = {
    serial = new Serial(this, port, baud)
    try {
      Thread.sleep(4000); // let bootloader timeout
    } catch {
      case e: InterruptedException =>
    }
    firmata.init() //
  }

  def serialEvent(which: Serial) {
    try {
      // Notify the Arduino class that there's serial data for it to process.
      while (which.available() > 0) {
        firmata.processInput(which.read())
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw new RuntimeException("Error inside Arduino.serialEvent()")
    }
  }

  class FirmataWriter extends Firmata.Writer {
    def write(value: Int) {
      serial.write(value)
    }
  }

  def analogWrite(pin: Int, value: Int) = firmata.analogWrite(pin, value)

  def analogRead(pin: Int) = firmata.analogRead(pin)

  def digitalWrite(pin: Int, value: Int) = firmata.digitalWrite(pin, value)

  def digitalRead(pin: Int) = firmata.digitalRead(pin)

  def servoWrite(pin: Int, value: Int) = firmata.servoWrite(pin, value)

  def pinMode(pin: Int, mode: Int) = firmata.pinMode(pin, mode)

}

class ArduinoActor(var port: String, var baud: Int) extends Actor with ActorLogging {
  def this() = this(Serial.list().head, 56700)

  def this(baud: Int) = this(Serial.list().head, baud)

  var arduino = ArduinoObj(port, baud)

  override def postStop() = {
    try {
      if (arduino.serial != null) arduino.serial.dispose()
    } finally {
      log.debug("postStop")
      context.parent ! Closed
    }
  }

  override def receive = {
    case Open =>
      log.debug("Inicializando")
      arduino.open
      log.debug("inicializado")
      sender ! Opened
      context.become(opened())
  }

  def opened(): Receive = {
    case Open => sender ! Opened
    case AnalogRead(pin: Int) =>
      val value = arduino.analogRead(pin)
      sender ! SensorReaded(pin, value)
      log.debug(s"Analog read. pin $pin, value $value")
    case PinMode(pin, pinMode) =>
      arduino.pinMode(pin, pinMode.mode)
      log.debug(s"board pin mode $pin, $pinMode" + pinMode.mode)
    case DigitalRead(pin: Int) => sender ! SensorReaded(pin, arduino.digitalRead(pin))
    case AnalogWrite(pin: Int, value: Int) => arduino.analogWrite(pin, value);
    case DigitalWrite(pin: Int, digitalValue: DigitalValue) =>
      log.debug(s"Digital write. pin $pin, value $digitalValue.value")
      arduino.digitalWrite(pin, digitalValue.value)
    case ServoWrite(pin: Int, value: Int) =>
      log.debug(s"servoWrite $pin, $value")
      arduino.servoWrite(pin, value)
    case Close => context.stop(self)
  }
}

