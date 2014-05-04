package com.adthan.ralphbot.core

import akka.actor._
import scala.concurrent.duration._
import akka.actor.ActorLogging
import com.adthan.firmakka._
import com.adthan.firmakka.messages._

case object OpenBoard

case object CloseBoard

case object BoardOpened

case object ReadIR

//case class ReadIR(sender: ActorRef, interval: Duration)
case class Distance(value: Double)

case object ArmInc
case object ArmDec
case object PanInc
case object PanDec
case object TiltInc
case object TiltDec

case object Forward
case object Reverse
case class MoveTilt(pos: Int)

case class MovePan(pos: Int)

case class MoveArm(pos: Int)

case class Status(var vel: Int, var dir: String, var pan: Int, var tilt: Int, var arm: Int, var ir: Double = 0)

class Board extends Actor with Stash with ActorLogging {

  val irPin: Int = 3
  val armPin: Int = 6
  val panPin: Int = 2
  val tiltPin: Int = 4

  val VOLTS_PER_UNIT = .0049F
  var ir, pan, tilt, arm: ActorRef = null
  var status = new Status(120, "parado", 90, 90, 90)
  val arduino = context.system.actorOf(Props(classOf[ArduinoActor]), "arduino")

  override def receive = {
    case OpenBoard =>
      arduino ! Open
    case Opened =>
      log.info("Connected to arduino" + arduino.path)
      initSensors()
      initServos()
      context become opened
      context.parent ! BoardOpened
      unstashAll()
    case FailToOpen(error) =>
      log.error(s"Could not connect to arduino: $error")
      context stop self
    case other => stash()
  }

  def opened: Receive = {
    case OpenBoard =>
      sender ! BoardOpened
    case CloseBoard =>
      log.info("Closing")
      arduino ! Close
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
    case ServoMoved(pin, value) =>
      log.debug(s"Servo moved $pin, $value")
      pin match {
        case `armPin` =>
          status.arm = value
          log.debug("---------------------------------------------- arm")
        case `panPin` =>
          status.pan = value
          log.debug("---------------------------------------------- pan")
        case `tiltPin` =>
          status.tilt = value
          log.debug("---------------------------------------------- tilt")
      }
      publishStatus()
    case SensorReaded(pin, value) =>
      status.ir = toCm(value)
      publishStatus()
    case Closed =>
      println("Serial port closed")
      context stop self
      sender ! Closed
    //  sender ! status
    case other => stash()
  }

  def publishStatus() {
    context.system.eventStream.publish(status)
  }

  def toCm(value: Int): Double = {
    //return 9462/(value - 16.92)

    var volts = value * VOLTS_PER_UNIT;
    // ("proxSens" is from analog read)
    //System.err.println("" + volts)
    //    var volts =  value * 0.0048828125 // value from sensor * (5/1024) - if running 3.3.volts then change 5 to 3.3
    var cm = 65 * Math.pow(volts, -1.10)
    //var cm = 60.495 * Math.pow(volts, -1.1904); // same in cm
    if (volts < .2) cm = -1.0;
    //System.err.println("" + cm)
    return cm;
  }

  def initSensors() = {
    ir = context.actorOf(Props(classOf[Sensor], irPin, 5500.milliseconds), "sensorIR")
  }


  def initServos() = {
    arm = context.actorOf(Props(classOf[Servo], armPin), "arm")
    pan = context.actorOf(Props(classOf[Servo], panPin), "pan")
    tilt = context.actorOf(Props(classOf[Servo], tiltPin), "tilt")
  }
}