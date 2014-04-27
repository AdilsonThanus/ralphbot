package com.adilsonthanus.ralphbot.core

import akka.actor._
import scala.concurrent.duration._
import akka.actor.ActorLogging
import com.adthan.firmata._
import com.adthan.firmata.messages._

case object OpenBoard
case object CloseBoard
case class BoardOpened(arduino: ActorRef)
case object ReadIR
//case class ReadIR(sender: ActorRef, interval: Duration)
case class Distance(value: Double)
case object ArmInc
case object ArmDec
case object PanInc
case object PanDec
case object TiltInc
case object TiltDec
case class MoveTilt(pos: Int)
case class MovePan(pos: Int)
case class MoveArm(pos: Int)
case class Status(var velocidade: Int, var direcao: String, var pan: Int, var tilt: Int, var arm: Int, var ir: Double = 0)

//import com.adilsonthanus.sjf.Arduino
//import com.adilsonthanus.sjf.Serial

//import cc.arduino.Arduino
//import processing.core.PApplet
//import processing.serial.Serial

class Board extends Actor with Stash with ActorLogging { //with Pantilt with Arm 
  import context.system

  val VOLTS_PER_UNIT = .0049F
  var ir, pan, tilt, arm: ActorRef = null
  var status = new Status(120, "parado", 90, 90, 90)
  val arduino = context.actorOf(Props(classOf[ArduinoActor]), "arduino")
  // def dispose = arduino ! Close

  override def receive = {
    case OpenBoard => {
      arduino ! Open
    }
    case Opened(arduino) => {
      log.info("Connected to port")
      initSensors
      initServos
      //    context become open(arduino)
       sender ! BoardOpened(self)
      unstashAll()
    }
    case FailToOpen(error) => {
      log.error(s"Could not connect to port: $error")
      context stop self
    }
    case CloseBoard => {
      log.info("Closing")
      arduino ! Close
    }
    case ReadIR => ir ! SensorRead
    case ArmInc => arm ! ServoInc
    case ArmDec => arm ! ServoDec
    case PanInc => pan ! ServoInc
    case PanDec => pan ! ServoDec
    case TiltInc => tilt ! ServoInc
    case TiltDec => tilt ! ServoDec
    case MovePan(pos) => pan ! ServoMove(pos)
    case MoveTilt(pos) => tilt ! ServoMove(pos)
    case MoveArm(pos) => arm ! ServoMove(pos)

    case ReadedValue(pin, value) => {
      status.ir = toCm(value);
      context.system.eventStream.publish(status)
    }
    case Closed => {
      println("Serial port closed")
      context stop self
      sender ! Closed
    }
    //  sender ! status
    case other => stash()
  }

  def toCm(value: Int): Double = {
    //return 9462/(value - 16.92)

    var volts = value * VOLTS_PER_UNIT; // ("proxSens" is from analog read)
    //System.err.println("" + volts)
    //    var volts =  value * 0.0048828125 // value from sensor * (5/1024) - if running 3.3.volts then change 5 to 3.3
    var cm = 65 * Math.pow(volts, -1.10)
    //var cm = 60.495 * Math.pow(volts, -1.1904); // same in cm
    if (volts < .2) cm = -1.0;
    //System.err.println("" + cm)
    return cm;
  }
  def initSensors = {
    ir = context.actorOf(Props(classOf[Sensor], arduino, 3, 30500.milliseconds), "sensorIR")
  }
  def initServos = {
    arm = context.actorOf(Props(classOf[Servo], arduino, 6), "arm")
    pan = context.actorOf(Props(classOf[Servo], arduino, 2), "pan")
    tilt = context.actorOf(Props(classOf[Servo], arduino, 4), "tilt")
  }

  //  def open: Receive = {
  //    case "" => {} //ReadIR => ir ! ReadIR(sender, 250.milliseconds)
  //  }
}