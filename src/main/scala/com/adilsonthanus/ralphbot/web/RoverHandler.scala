package com.adilsonthanus.ralphbot.web

import akka.actor.{ Actor, ActorLogging }
import akka.actor.actorRef2Scala
import akka.actor.ActorRef
import com.adilsonthanus.ralphbot.core._
import org.mashupbots.socko.events.WebSocketFrameEvent
import com.adilsonthanus.ralphbot.RalphBotApp
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{ read, write, writePretty }

case class CommandMsg(var command:String, var params:List[String], var callback_id:Int)

class RoverHandler extends Actor with ActorLogging {

  implicit val formats = DefaultFormats
  type DslConversion = CommandMsg => JValue

  def actorRefFactory = context
  var board: ActorRef = actorRefFactory.actorFor("/user/roverBoard") //= new Board

  override def preStart() {
    // board = context.actorOf(Props(classOf[Board]), "roverBoard")
	  context.system.eventStream.subscribe(self,classOf[Status])
  }
  /**
   * Process incoming messages
   */
  def receive = {
    case status: Status => {
      log.info("Receive status "+ status)

      RalphBotApp.webServer.webSocketConnections.writeText(status.toString);
      //var statusJSON = status.toJson.compactPrint
      //clients foreach (ws => ws.send("{ \"data\":{ " + statusJSON + "}}"))
      //clients foreach (ws => ws.send(statusJSON))
      //clients foreach (ws => ws.send("{ \"data\":" + statusJSON + ", \"callback_id\": 0,\"result\":true}"))
    }
    case BoardOpened(board: ActorRef) => {
      log.info("Opened  RocketSocket")
      //context become open(board)
      board ! ReadIR
    }
    case event: WebSocketFrameEvent =>
      // Echo web socket text frames
      handleWebSocketResponse(event)
    //context.stop(self)
    case _ => {
      log.info("received unknown message of type: ")
      //context.stop(self)
    }
  }

  def handleWebSocketResponse(event: WebSocketFrameEvent) = {

    val text = event.readText
    log.info(text)
    val parsed = parse(text)
    log.info(parsed.toString)
    //..foreach(x => log.info(String.valueOf(x)))
    var commandMsg = parsed.extract[CommandMsg]
//    var msg = parse(event.readText)
    log.info("===========================================")
    log.info("Message ==> " + commandMsg)
    log.info(board.toString)
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
      case CommandMsg("armInc",_,_) => board ! ArmInc
      case CommandMsg("armDec",_,_) => board ! ArmDec
      case CommandMsg("panMove",List(value),_) => board ! MovePan(value.toInt)
      case CommandMsg("tiltMove",List(value),_) => board ! MoveTilt(value.toInt)
      case CommandMsg("armMove",List(value),_) => board ! MoveArm(value.toInt)
    }
//    if (msg.contains("panInc")) board ! PanInc
//    if (msg.contains("panDec")) board ! PanDec
//    if (msg.contains("tiltInc")) board ! TiltInc
//    if (msg.contains("tiltDec")) board ! TiltDec

  }
}