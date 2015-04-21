package com.adthan.firmakka

import akka.actor.Actor
import akka.actor.ActorLogging
import com.adthan.firmakka.messages._

import com.adthan.firmakka.sjf.Serial

import com.adthan.firmakka.sjf.Arduino

//import com.adthan.firmakka.sjfscala.Firmata

//import com.adthan.firmakka.sjfscala.Serial
//import com.adthan.firmakka.sjfscala

class ArduinoActor() extends Actor with ActorLogging {

  var arduino: Arduino = null;

  //  override def postStop() = {
  //    try {
  //      if (arduino.serial != null) arduino.serial.stop()
  //    } finally {
  //      log.info("postStop")
  //      context.parent ! Closed
  //    }
  //  }

  override def receive = {
    case Open =>
      log.info("Inicializando")
      var porta = Serial.list.head
      log.warning(porta)
      porta = "/dev/ttyUSB0"
      //if (portas.isEmpty) {
      //  porta = portas.head
      //}
      log.warning(porta)
      arduino = new Arduino(porta, 57600)//, 115200)
      log.info("inicializado")
      context.become(opened())
      sender ! Opened
  }

  def opened(): Receive = {
    case Open => sender ! Opened
    case AnalogRead(pin: Int) =>
      val value = arduino.analogRead(pin)
      sender ! SensorReaded(pin, value)
      //log.info(s"Analog read. pin $pin, value $value")
    case PinMode(pin, pinMode) =>
      arduino.pinMode(pin, pinMode.mode)
      //log.info(s"board pin mode $pin, $pinMode" + pinMode.mode)
    case DigitalRead(pin: Int) => sender ! SensorReaded(pin, arduino.digitalRead(pin))
    case AnalogWrite(pin: Int, value: Int) => arduino.analogWrite(pin, value);
    case DigitalWrite(pin: Int, digitalValue: DigitalValue) =>
      //log.info(s"Digital write. pin $pin, value $digitalValue.value")
      arduino.digitalWrite(pin, digitalValue.value)
    case ServoWrite(pin: Int, value: Int) =>
      //log.info(s"servoWrite $pin, $value")
      arduino.servoWrite(pin, value)
    case PulseIn(pin: Int, value: DigitalValue, pulseOut: Int, timeout: Int) => arduino.pulseIn(pin, value, pulseOut, timeout)
    case PulseInRead(pin: Int) =>
      val value = arduino.pulseInRead(pin)
      sender ! SensorReaded(pin, value)
      //log.info(s"Ping read. pin $pin, value $value")
    case Close => context.stop(self)
  }
}

