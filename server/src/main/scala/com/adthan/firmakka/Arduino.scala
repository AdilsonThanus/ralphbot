package com.adthan.firmakka

import com.adthan.firmakka.messages.DigitalValue
import com.adthan.firmakka.sjf.Serial
import com.adthan.firmakka.sjf.Firmata

//import com.adthan.firmakka.sjfscala.Firmata
class Arduino {

  final val START_SYSEX = 0xF0
  final val END_SYSEX = 0xF7
  final val PULSE_OUT = 0x73
  final val PULSE_IN = 0x74
  final val INPUT: Int = 0
  final val OUTPUT: Int = 1
  final val ANALOG: Int = 2
  final val PWM: Int = 3
  final val SERVO: Int = 4
  final val SHIFT: Int = 5
  final val I2C: Int = 6
  final val LOW: Int = 0
  final val HIGH: Int = 1

  var serial: Serial = null
  var serialProxy: Arduino#SerialProxy = null
  var firmata: Firmata = null

  def list: Array[String] = {
    return Serial.list
  }


  class SerialProxy {

    def serialEvent(which: Serial) {
      try {
        while (which.available > 0) firmata.processInput(which.read)
      }
      catch {
        case e: Exception => {
          e.printStackTrace
          throw new RuntimeException("Error inside Arduino.serialEvent()")
        }
      }
    }
  }

  class FirmataWriter extends Firmata.Writer {
    def write(`val`: Int) {
      serial.write(`val`)
    }
  }

//    def dispose {
//      this.serial.dispose
//    }

  def this(iname: String, irate: Int) {
    this()
    println("arduino scala")
    this.firmata = new Firmata(new FirmataWriter)
    this.serialProxy = new SerialProxy
    this.serial = new Serial(serialProxy, iname, irate)
    try {
      Thread.sleep(6000)
    }
    catch {
      case e: InterruptedException => {
      }
    }
    firmata.init
  }

  def digitalRead(pin: Int): Int = {
    return firmata.digitalRead(pin)
  }

  def analogRead(pin: Int): Int = {
    return firmata.analogRead(pin)
  }

  def pinMode(pin: Int, mode: Int) {
    try {
      firmata.pinMode(pin, mode)
    }
    catch {
      case e: Exception => {
        e.printStackTrace
        throw new RuntimeException("Error inside Arduino.pinMode()")
      }
    }
  }

  def pulseInRead(pin: Int) = firmata.pulseInRead(pin)

  def pulseIn(pin: Int, digitalValue: DigitalValue, pulseOut: Int = 0, timeout: Int = 1000000) {

    var pulseOutArray = Array[Int](
      (pulseOut >> 24) & 0xFF,
      (pulseOut >> 16) & 0xFF,
      (pulseOut >> 8) & 0xFF,
      pulseOut & 0xFF
    )
    var timeoutArray = Array[Int](
      (timeout >> 24) & 0xFF,
      (timeout >> 16) & 0xFF,
      (timeout >> 8) & 0xFF,
      timeout & 0xFF
    )
    var data = Array[Int](
      START_SYSEX,
      PULSE_IN,
      pin,
      digitalValue.value,
      pulseOutArray(0) & 0x7F,
      (pulseOutArray(0) >> 7) & 0x7F,
      pulseOutArray(1) & 0x7F,
      (pulseOutArray(1) >> 7) & 0x7F,
      pulseOutArray(2) & 0x7F,
      (pulseOutArray(2) >> 7) & 0x7F,
      pulseOutArray(3) & 0x7F,
      (pulseOutArray(3) >> 7) & 0x7F,
      timeoutArray(0) & 0x7F,
      (timeoutArray(0) >> 7) & 0x7F,
      timeoutArray(1) & 0x7F,
      (timeoutArray(1) >> 7) & 0x7F,
      timeoutArray(2) & 0x7F,
      (timeoutArray(2) >> 7) & 0x7F,
      timeoutArray(3) & 0x7F,
      (timeoutArray(3) >> 7) & 0x7F,
      END_SYSEX
    )
    data.foreach(serial.write)
    //this.sp.write(new Buffer(data));
    //this.once('pulse - in - ' +pin, callback);
  }

  /**
   * Write to a digital pin (the pin must have been put into output mode with
   * pinMode()).
   *
   * @param pin
	 * the pin to write to (from 2 to 13)
   * @param value
	 * the value to write: Arduino.LOW (0 volts) or Arduino.HIGH (5
   * volts)
   */
  def digitalWrite(pin: Int, value: Int) {
    try {
      firmata.digitalWrite(pin, value)
    }
    catch {
      case e: Exception => {
        e.printStackTrace
        throw new RuntimeException("Error inside Arduino.digitalWrite()")
      }
    }
  }

  /**
   * Write an analog value (PWM-wave) to a digital pin.
   *
   * @param pin
	 * the pin to write to (must be 9, 10, or 11, as those are they
   * only ones which support hardware pwm)
   * @param value
	 * the value: 0 being the lowest (always off), and 255 the
   * highest (always on)
   */
  def analogWrite(pin: Int, value: Int) {
    try {
      firmata.analogWrite(pin, value)
    }
    catch {
      case e: Exception => {
        e.printStackTrace
        throw new RuntimeException("Error inside Arduino.analogWrite()")
      }
    }
  }

  /**
   * Write a value to a servo pin.
   *
   * @param pin
	 * the pin the servo is attached to
   * @param value
	 * the value: 0 being the lowest angle, and 180 the highest angle
   */
  def servoWrite(pin: Int, value: Int) {
    try {
      firmata.servoWrite(pin, value)
    }
    catch {
      case e: Exception => {
        e.printStackTrace
        throw new RuntimeException("Error inside Arduino.servoWrite()")
      }
    }
  }


}