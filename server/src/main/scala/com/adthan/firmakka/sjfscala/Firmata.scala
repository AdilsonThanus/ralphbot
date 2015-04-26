/**
 * Firmata.java - Firmata library for Java
 * Copyright (C) 2006-13 David A. Mellis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * Java code to communicate with the Arduino Firmata 2 firmware.
 * http://firmata.org/
 *
 * $Id$
 */
package com.adthan.firmakka.sjfscala

object Firmata {

  trait Writer {
    def write(value: Int)
  }

}

class Firmata {
  final val INPUT: Int = 0
  final val OUTPUT: Int = 1
  final val ANALOG: Int = 2
  final val PWM: Int = 3
  final val SERVO: Int = 4
  final val SHIFT: Int = 5
  final val I2C: Int = 6
  final val LOW: Int = 0
  final val HIGH: Int = 1
  private final val MAX_DATA_BYTES: Int = 4096
  private final val DIGITAL_MESSAGE: Int = 0x90
  private final val ANALOG_MESSAGE: Int = 0xE0
  private final val REPORT_ANALOG: Int = 0xC0
  private final val REPORT_DIGITAL: Int = 0xD0
  private final val SET_PIN_MODE: Int = 0xF4
  private final val REPORT_VERSION: Int = 0xF9
  private final val SYSTEM_RESET: Int = 0xFF
  private final val START_SYSEX: Int = 0xF0
  private final val END_SYSEX: Int = 0xF7
  private final val SERVO_CONFIG: Int = 0x70
  private final val STRING_DATA: Int = 0x71
  private final val SHIFT_DATA: Int = 0x75
  private final val I2C_REQUEST: Int = 0x76
  private final val I2C_REPLY: Int = 0x77
  private final val I2C_CONFIG: Int = 0x78
  private final val EXTENDED_ANALOG: Int = 0x6F
  private final val PIN_STATE_QUERY: Int = 0x6D
  private final val PIN_STATE_RESPONSE: Int = 0x6E
  private final val CAPABILITY_QUERY: Int = 0x6B
  private final val CAPABILITY_RESPONSE: Int = 0x6C
  private final val ANALOG_MAPPING_QUERY: Int = 0x69
  private final val ANALOG_MAPPING_RESPONSE: Int = 0x6A
  private final val REPORT_FIRMWARE: Int = 0x79
  private final val SAMPLING_INTERVAL: Int = 0x7A
  private final val SYSEX_NON_REALTIME: Int = 0x7E
  private final val SYSEX_REALTIME: Int = 0x7F
  private final val PULSE_IN: Int = 0x74
  private final val RESET: Int = 0xFF
  private[firmakka] var waitForData: Int = 0
  private[firmakka] var executeMultiByteCommand: Int = 0
  private[firmakka] var multiByteChannel: Int = 0
  private[firmakka] var storedInputData: Array[Int] = new Array[Int](MAX_DATA_BYTES)
  private[firmakka] var parsingSysex: Boolean = false
  private[firmakka] var sysexBytesRead: Int = 0
  private[firmakka] var digitalOutputData: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  private[firmakka] var digitalInputData: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  private[firmakka] var analogInputData: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  private[firmakka] var pulseInData: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  private final val MAX_PINS: Int = 128
  private[firmakka] var pinModes: Array[Int] = new Array[Int](MAX_PINS)
  private[firmakka] var analogChannel: Array[Int] = new Array[Int](MAX_PINS)
  private[firmakka] var pinMode: Array[Int] = new Array[Int](MAX_PINS)
  private[firmakka] var majorVersion: Int = 0
  private[firmakka] var minorVersion: Int = 0
  private[firmakka] var out: Firmata.Writer = null

  def this(writer: Firmata.Writer) {
    this()
    this.out = writer
  }

  def init() = {

    println("init")
    (0 to 15).foreach({ i =>
      val x = REPORT_DIGITAL | i
      println(s"i $i   ,   $REPORT_DIGITAL = $x")
      out.write(REPORT_DIGITAL | i)
      out.write(i)
    })
    queryAnalogMapping()
  }

  def pulseInRead(pin: Int): Int = pulseInData(pin)

  def digitalRead(pin: Int): Int = (digitalInputData(pin >> 3) >> (pin & 0x07)) & 0x01

  def analogRead(pin: Int): Int = analogInputData(pin)


  def reset() {
    out.write(RESET)
  }

  def pinMode(pin: Int, mode: Int) {
    out.write(SET_PIN_MODE)
    out.write(pin)
    out.write(mode)
  }

  def digitalWrite(pin: Int, value: Int) {
    val portNumber: Int = (pin >> 3) & 0x0F
    if (value == 0) digitalOutputData(portNumber) &= ~(1 << (pin & 0x07))
    else digitalOutputData(portNumber) |= (1 << (pin & 0x07))
    out.write(DIGITAL_MESSAGE | portNumber)
    out.write(digitalOutputData(portNumber) & 0x7F)
    out.write(digitalOutputData(portNumber) >> 7)
  }

  def analogWrite(pin: Int, value: Int) {
    pinMode(pin, PWM)
    out.write(ANALOG_MESSAGE | (pin & 0x0F))
    out.write(value & 0x7F)
    out.write(value >> 7)
  }

  def servoWrite(pin: Int, value: Int) {
    out.write(ANALOG_MESSAGE | (pin & 0x0F))
    out.write(value & 0x7F)
    out.write(value >> 7)
  }

  def setDigitalInputs(portNumber: Int, portData: Int) {
    digitalInputData(portNumber) = portData
  }

  def setAnalogInput(pin: Int, value: Int) {
    analogInputData(pin) = value
  }

  def setVersion(majorVersion: Int, minorVersion: Int) {
    println(s"version $majorVersion.$minorVersion  ")

    this.majorVersion = majorVersion
    this.minorVersion = minorVersion
  }

  def queryCapabilities() = {
    out.write(START_SYSEX)
    out.write(CAPABILITY_QUERY)
    out.write(END_SYSEX)
  }

  def queryAnalogMapping() {
    out.write(START_SYSEX)
    out.write(ANALOG_MAPPING_QUERY)
    out.write(END_SYSEX)
  }

  def processSysexMessage() {
    println("======== processSysexMessage =========")
    println(storedInputData.foreach(c => print(c + " ")))
    println(storedInputData(0))
    storedInputData(0) match {
      case PULSE_IN =>
        //println(s"PULSE IN:  $storedInputData")
        val pinPulse = (storedInputData(1) & 0x7F) | ((storedInputData(2) & 0x7F) << 7)
        val durationBuffer = Array[Int](
          (storedInputData(3) & 0x7F) |
            ((storedInputData(4) & 0x7F) << 7),
          (storedInputData(5) & 0x7F) | ((storedInputData(6) & 0x7F) << 7),
          (storedInputData(7) & 0x7F) | ((storedInputData(8) & 0x7F) << 7),
          (storedInputData(9) & 0x7F) | ((storedInputData(10) & 0x7F) << 7))
        val duration = (durationBuffer(0) << 24) +
          (durationBuffer(1) << 16) +
          (durationBuffer(2) << 8) +
          durationBuffer(3)
        pulseInData(pinPulse) = duration
      case ANALOG_MAPPING_RESPONSE =>
        println(s"ANALOG_MAPPING_RESPONSE:   $storedInputData")

        //          var pin: Int = 0
        //          while (pin < analogChannel.length) {
        //            analogChannel(pin) = 127
        //            ({
        //              pin += 1; pin - 1
        //            })
        //          }
        //
        //
        //          var i: Int = 1
        //          while (i < sysexBytesRead) {
        //            analogChannel(i - 1) = storedInputData(i)
        //            ({
        //              i += 1; i - 1
        //            })
        //          }
        //
        //        {
        //          var pin: Int = 0
        //          while (pin < analogChannel.length) {
        //            {
        //              if (analogChannel(pin) != 127) {
        //                out.write(REPORT_ANALOG | analogChannel(pin))
        //                System.out.println(REPORT_ANALOG | analogChannel(pin))
        //                out.write(1)
        //              }
        //            }
        //            ({
        //              pin += 1; pin - 1
        //            })
        //          }
        //        }

        //
        (0 to analogChannel.length - 1).foreach(i => analogChannel(i) = 127)
        (1 to sysexBytesRead - 1).foreach(i => analogChannel(i - 1) = storedInputData(i))
        (0 to analogChannel.length - 1).foreach(pin => {
          if (analogChannel(pin) != 127) {
            out.write(REPORT_ANALOG | analogChannel(pin))
            println(REPORT_ANALOG | analogChannel(pin))
            out.write(1)
          }
        })
        print("[")
        analogChannel.foreach(c => {
          print(c + " ")
        })
        println("]")
      case _ => {
        println(s"======== processSysexMessage IGNORADA =========: $ANALOG_MAPPING_RESPONSE")

        println(storedInputData.foreach(c => print(c + " ")))
        println(storedInputData(0))
      }

    }
  }

  def processInput(inputData: Int) {
    if (parsingSysex) {
      if (inputData == END_SYSEX) {
        parsingSysex = false
        processSysexMessage
      }
      else {
        storedInputData(sysexBytesRead) = inputData
        sysexBytesRead += 1
      }
    }
    else if (waitForData > 0 && inputData < 128) {
      waitForData -= 1
      storedInputData(waitForData) = inputData
      if (executeMultiByteCommand != 0 && waitForData == 0) {
        executeMultiByteCommand match {
          case DIGITAL_MESSAGE =>
            setDigitalInputs(multiByteChannel, (storedInputData(0) << 7) + storedInputData(1))
          case ANALOG_MESSAGE =>
            System.out.println("multiByteChannel " + multiByteChannel + ">>" + storedInputData(0) + "   " + storedInputData(1))
            setAnalogInput(multiByteChannel, (storedInputData(0) << 7) + storedInputData(1))
          case REPORT_VERSION =>
            setVersion(storedInputData(1), storedInputData(0))
        }
      }
    }
    else {
      var command: Int = -1
      if (inputData < 0xF0) {
        command = inputData & 0xF0
        multiByteChannel = inputData & 0x0F
      } else  command = inputData
      command match {
        case DIGITAL_MESSAGE =>
        case ANALOG_MESSAGE =>
        case REPORT_VERSION =>
          waitForData = 2
          executeMultiByteCommand = command
        case START_SYSEX =>
          parsingSysex = true
          sysexBytesRead = 0
        case _ => println(s"ignorado : $command multi $multiByteChannel, executeMulti $executeMultiByteCommand")
      }
    }





    //
    //    //    println(storedInputData.foreach(c => print(c + " ")))
    //    //     println(inputData)
    //    var command: Int = 0
    //    if (parsingSysex) {
    //      if (inputData == END_SYSEX) {
    //        parsingSysex = false
    //        processSysexMessage()
    //      } else {
    //        storedInputData(sysexBytesRead) = inputData
    //        sysexBytesRead = sysexBytesRead+1
    //      }
    //    } else if (waitForData > 0 && inputData < 128) {
    //      waitForData = waitForData - 1
    //      storedInputData(waitForData) = inputData
    //      if (executeMultiByteCommand != 0 && waitForData == 0) {
    //        executeMultiByteCommand match {
    //          case DIGITAL_MESSAGE =>
    //            setDigitalInputs(multiByteChannel, (storedInputData(0) << 7) + storedInputData(1))
    //          case ANALOG_MESSAGE =>
    //            println(s"multiByteChannel $multiByteChannel, $storedInputData(0) , $storedInputData(1)")
    //            setAnalogInput(multiByteChannel, (storedInputData(0) << 7) + storedInputData(1))
    //          case REPORT_VERSION =>
    //            setVersion(storedInputData(1), storedInputData(0))
    //          case _ =>
    //        }
    //      }
    //    } else {
    //      if (inputData < 0xF0) {
    //        command = inputData & 0xF0
    //        multiByteChannel = inputData & 0x0F
    //        // println(s" multiByteChannel $inputData $multiByteChannel")
    //      } else command = inputData
    //      command match {
    //        case DIGITAL_MESSAGE =>
    //        case ANALOG_MESSAGE =>
    //        case REPORT_VERSION =>
    //          waitForData = 2
    //          executeMultiByteCommand = command
    //        case START_SYSEX =>
    //          parsingSysex = true
    //          sysexBytesRead = 0
    //        case _ =>
    //      }
    //    }
  }
}