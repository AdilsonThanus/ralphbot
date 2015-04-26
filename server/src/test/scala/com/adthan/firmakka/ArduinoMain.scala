package com.adthan.firmakka

import com.adthan.firmakka.sjfscala.Serial

//import com.adthan.firmakka.sjf.Arduino;
//import com.adthan.firmakka.sjf.Serial;
//import com.adthan.firmakka.sjf.Serial;
object ArduinoMain {
  def main(args: Array[String]) {
    println("arduino main scala")
    val arduino: Arduino = new Arduino(Serial.list(0), 57600)
    try {
      Thread.sleep(4000)
    }
    catch {
      case e: InterruptedException => {
        e.printStackTrace
      }
    }
    System.err.println(arduino.analogRead(3))
    System.err.println(arduino.analogRead(3))
    arduino.pinMode(2, 4)
    arduino.servoWrite(2, 120)
  }
}