package com.adthan.ralphbot.web

import akka.actor._
import com.adthan.ralphbot.core._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.mashupbots.socko.events.WebSocketFrameEvent
import akka.routing.FromConfig
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class CommandMsg(var command: String, var params: List[String], var callback_id: Int)

class RoverHandler extends Actor with ActorLogging {

  import context.system

  implicit val formats = DefaultFormats
  type DslConversion = CommandMsg => JValue


  var board: ActorRef = null

  override def preStart() {
    var future = system.actorSelection("/user/roverBoard").resolveOne(5.seconds)

    future.onSuccess({
      case currentBoard: ActorRef =>
        board = currentBoard
        board ! OpenBoard
    })
    future.onFailure({
      case _ =>
        board = system.actorOf(Props(classOf[Board]).
          withRouter(FromConfig()).withDispatcher("ralphbot-pinned-dispatcher"), "roverBoard")
        log.info("iniciando rover")
        board ! OpenBoard
    })

    // board = context.actorOf(Props(classOf[Board]), "roverBoard")
  }

  def receive = {
    case BoardOpened =>
      log.info("Opened  RocketSocket")
    //board ! ReadIR
    // context become opened
    //      context.stop(self)
    //
    //  }
    //
    //  /**
    //   * Process incoming messages
    //   */
    //  def opened: Receive = {
    case event: WebSocketFrameEvent => handleWebSocketResponse(event)
    // context.stop(self)
    case _ => log.info("received unknown message of type: ")
    // context.stop(self)
  }

  def handleWebSocketResponse(event: WebSocketFrameEvent) = {

    val commandMsg = parse(event.readText()).extract[CommandMsg]
    //    var msg = parse(event.readText)
    log.info("===========================================")
    log.info("Message ==> " + commandMsg)
    log.info("===========================================")

    //log.info("Distancia : " + board.readSensorIR)

    //      ws.send("Distancia : " + board.status.ir)

    // var jmsg:JSONObject = JSON.parseFull(msg).head.asInstanceOf[JSONObject];
    /*
      if (msg.contains("frente")) {
        board.frente
        ws.send("{ \"data\":{\"direcao\":\"frente\", \"velocidade\":" + board.status.velocidade + "}, \"callback_id\": 1,\"result\":true}")
        log.debug("frenter" + ws.getResourceDescriptor)
      }
      if (msg.contains("tras")) {
        board.tras
        ws.send("para tras. velocidade : " + board.status.velocidade)
        log.info("para tras " + ws.getResourceDescriptor)
      }
      if (msg.contains("direita")) {
        board.direita
        ws.send("para direita. velocidade : " + board.status.velocidade)
        log.info("para tras " + ws.getResourceDescriptor)
      }
      if (msg.contains("esquerda")) {
        board.esquerda
        ws.send("para esquerda. velocidade : " + board.status.velocidade)
        log.info("para tras " + ws.getResourceDescriptor)
      }
      if (msg.contains("parar")) {
        board.parar
        ws.send("parar")
        log.info("parando !" + ws.getResourceDescriptor)
      }
*/
    commandMsg match {
      case CommandMsg("armInc", _, _) => board ! ArmInc
      case CommandMsg("armDec", _, _) => board ! ArmDec
      case CommandMsg("panInc", _, _) => board ! PanInc
      case CommandMsg("panDec", _, _) => board ! PanDec
      case CommandMsg("tiltInc", _, _) => board ! TiltInc
      case CommandMsg("tiltDec", _, _) => board ! TiltDec
      case CommandMsg("panMove", List(value), _) => board ! MovePan(value.toInt)
      case CommandMsg("tiltMove", List(value), _) => board ! MoveTilt(value.toInt)
      case CommandMsg("armMove", List(value), _) => board ! MoveArm(value.toInt)
      case _ => log.info(s"Message not implemented : $commandMsg")
    }
    //    if (msg.contains("panInc")) board ! PanInc
    //    if (msg.contains("panDec")) board ! PanDec
    //    if (msg.contains("tiltInc")) board ! TiltInc
    //    if (msg.contains("tiltDec")) board ! TiltDec

  }
}