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

case object Stop

case object Left

case object Right

case object SlowDonw

case object Acelerate

case class Velocity(vel: Int)

case class MoveTilt(pos: Int)

case class MovePan(pos: Int)

case class MoveArm(pos: Int)

case class Status(var vel: Int, var dir: String, var pan: Int, var tilt: Int, var arm: Int, var ir: Double = 0, var distance: Double = 0)

class Board extends Actor with StatusBroadcast with Stash with ActorLogging with Motors with Servos with Sensors {

  val arduino = context.system.actorOf(Props(classOf[ArduinoActor]), "arduino")

  override def receive = {
    case OpenBoard =>
      arduino ! Open
    case Opened =>
      log.info("Connected to arduino" + arduino.path)
      //initSensors()
      try {
        Thread.sleep(3000)
      }
      catch {
        case e: InterruptedException => {
        }
      }
      initSensors()
      initServos()
      initMotors()
      unstashAll()
      context.become(opened, discardOld = false)
      context.parent ! BoardOpened
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
    // case ReadIR => ir ! SensorRead

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
        case `armPin` => status.arm = value
        case `panPin` => status.pan = value
        case `tiltPin` => status.tilt = value
      }
      publishStatus()
    case SensorReaded(pin, value) =>
      pin match {
        //case `irPin` => status.ir = irCm(value)
        case `pingPin` => status.distance = pingCm(value)
      }
      publishStatus()
    case Forward => forward()
    case Reverse => reverse()
    case Left => left()
    case Right => right()
    case Stop => stop()
    case Acelerate => acelerate()
    case SlowDonw => slowDown()
    case Closed =>
      println("Serial port closed")
      context stop self
      sender ! Closed
    //  sender ! status
    case _ => log.error("Invalid message")
  }


}


trait StatusBroadcast extends Actor {
  var status = new Status(250, "parado", 90, 90, 90)

  def publishStatus() {
    context.system.eventStream.publish(status)
  }
}

trait Sensors extends Actor {
 // val irPin: Int = 10
  val pingPin = 35
  val VOLTS_PER_UNIT = .0049F
  var ir, ping: ActorRef = null

  def initSensors() = {
   // ir = context.actorOf(Props(classOf[Sensor], irPin, 5500.milliseconds), "sensorIR")
    ping = context.actorOf(Props(classOf[Ping], pingPin), "sensorPing")
  }

  def irCm(value: Int): Double = {
    //return 9462/(value - 16.92)
    val volts = value * VOLTS_PER_UNIT
    // ("proxSens" is from analog read)
    //System.err.println("" + volts)
    //    var volts =  value * 0.0048828125 // value from sensor * (5/1024) - if running 3.3.volts then change 5 to 3.3
    if (volts < .2) -1.0
    65 * Math.pow(volts, -1.10)
    //var cm = 60.495 * Math.pow(volts, -1.1904); // same in cm
    //System.err.println("" + cm)
  }
  def pingCm(value: Int): Double = value / 29 / 2
}

trait Servos extends Actor {
  val armPin: Int = 22
  val panPin: Int = 20
  val tiltPin: Int = 21
  var pan, tilt, arm: ActorRef = null

  def initServos() = {
    arm = context.actorOf(Props(classOf[Servo], armPin), "arm")
    // pan = context.actorOf(Props(classOf[Servo], panPin), "pan")
    // tilt = context.actorOf(Props(classOf[Servo], tiltPin), "tilt")
  }
}

trait Motors extends Actor with StatusBroadcast {
  //  motorFrenteEsquerda = new five.Motor([3, 2]);// canal 1
  //  motorTraseiraEsquerda = new five.Motor([5, 4]);// canal 2
  //  motorFrenteDireita = new five.Motor([6, 7]); // canal 3
  //  motorTraseiraDireita = new five.Motor([9, 8]); // canal 4

  // (vel, dir, cur) canal 1
  val motorFrontLeftPins = MotorPins(2, 30)
  // canal 2
  val motorBackLeftPins = MotorPins(3, 31)
  // canal 3
  val motorFrontRightPins = MotorPins(4, 32)
  // canal 4
  val motorBackRightPins = MotorPins(5, 33)

  var motorFrontRight, motorFrontLeft, motorBackRight, motorBackLeft: ActorRef = null

  def initMotor(motorPins: MotorPins) = context.actorOf(Props(classOf[Motor], motorPins))


  def acelerate() = {
    status.vel += 10
    publishStatus()
  }

  def slowDown() = {
    status.vel -= 10
    publishStatus()
  }

  def initMotors() = {
    motorFrontRight = initMotor(motorFrontRightPins)
    motorFrontLeft = initMotor(motorFrontLeftPins)
    motorBackRight = initMotor(motorBackRightPins)
    motorBackLeft = initMotor(motorBackLeftPins)
  }

  def reverse() = {
    motorFrontRight ! MotorForward(status.vel)
    motorFrontLeft ! MotorForward(status.vel)
    motorBackRight ! MotorReverse(status.vel)
    motorBackLeft ! MotorReverse(status.vel)
    status.dir = "tras"
    publishStatus()
  }

  def forward() = {
    motorFrontRight ! MotorReverse(status.vel)
    motorFrontLeft ! MotorReverse(status.vel)
    motorBackRight ! MotorForward(status.vel)
    motorBackLeft ! MotorForward(status.vel)
    status.dir = "frente"
    publishStatus()
  }

  def stop() = {
    motorFrontRight ! MotorStop
    motorFrontLeft ! MotorStop
    motorBackRight ! MotorStop
    motorBackLeft ! MotorStop
    status.dir = "parado"
    publishStatus()
  }

  def right() = {
    motorFrontRight ! MotorForward(status.vel)
    motorFrontLeft ! MotorReverse(status.vel)
    motorBackRight ! MotorReverse(status.vel)
    motorBackLeft ! MotorForward(status.vel)
    status.dir = "direita"
    publishStatus()
  }

  def left() = {
    motorFrontRight ! MotorReverse(status.vel)
    motorFrontLeft ! MotorForward(status.vel)
    motorBackRight ! MotorForward(status.vel)
    motorBackLeft ! MotorReverse(status.vel)
    status.dir = "esquerda"
    publishStatus()
  }

  def velocity(vel: Int) = {
    motorFrontRight ! Velocity(vel)
    motorFrontLeft ! Velocity(vel)
    motorBackRight ! Velocity(vel)
    motorBackLeft ! Velocity(vel)
    status.vel = vel
    publishStatus()
  }

}