package com.adthan.ralphbot.core

import akka.actor._
import com.adthan.ralphbot.RalphBotApp
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

class Autonomus extends Actor with ActorLogging {

  context.system.eventStream.subscribe(self, classOf[Status])
  type DslConversion = Status => JValue
  implicit val formats = Serialization.formats(NoTypeHints)

  def receive = {
    case status: Status =>
      //log.info("Receive status " + status)
      var statusJSON = write(status)
      RalphBotApp.webServer.webSocketConnections.writeText(statusJSON)
    //clients foreach (ws => ws.send("{ \"data\":{ " + statusJSON + "}}"))
    //clients foreach (ws => ws.send(statusJSON))
    //clients foreach (ws => ws.send("{ \"data\":" + statusJSON + ", \"callback_id\": 0,\"result\":true}"))
  }
}