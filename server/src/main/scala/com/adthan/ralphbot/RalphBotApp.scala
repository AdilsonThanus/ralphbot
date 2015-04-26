package com.adthan.ralphbot

import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig
import akka.actor.ActorSystem
import akka.actor.Props
import java.io.File
import org.mashupbots.socko.handlers.{StaticResourceRequest, StaticContentHandlerConfig, StaticContentHandler}
import com.typesafe.config.ConfigFactory
import com.adthan.ralphbot.web.{StatusActor, RoverHandler}
import akka.routing.FromConfig
import com.adthan.ralphbot.core.Board
import com.adthan.ralphbot.core.OpenBoard

object RalphBotApp extends Logger {

  //log.info(this.getClass().getClassLoader().getResource("web").toString);

  //  val contentDir = new File("classpath:/web/")
  //val contentDir = new File(Thread.currentThread().getContextClassLoader().getResource("web").getPath())
  // log.info(contentDir.getAbsolutePath)
  //.info(contentDir.getPath)
  val tempDir = createTempDir("temp_")
  val staticContentHandlerConfig = StaticContentHandlerConfig(
    //rootFilePaths = Seq(contentDir.getPath()),
    tempDir = tempDir)

  //
  // STEP #1 - Define Actors and Start Akka
  //
  // We are going to start StaticContentHandler actor as a router.
  // There will be 5 instances, each instance having its own thread since there is a lot of blocking IO.

  //  val actorSystem = ActorSystem("RalphBotActorSystem", ConfigFactory.parseString(actorConfig))

  val actorSystem = ActorSystem("RalphBotActorSystem")
  actorSystem.actorOf(Props[StatusActor])

  var roverHandler = actorSystem.actorOf(Props(classOf[RoverHandler]).
    withRouter(FromConfig()).withDispatcher("ralphbot-pinned-dispatcher"), "roverHandler")

  val board = actorSystem.actorOf(Props(classOf[Board]).
    withRouter(FromConfig()).withDispatcher("ralphbot-pinned-dispatcher"), "roverBoard")
  log.info("iniciando rover")
  board ! OpenBoard

  val staticContentHandlerRouter = actorSystem.actorOf(Props(new StaticContentHandler(staticContentHandlerConfig))
    .withRouter(FromConfig()).withDispatcher("ralphbot-pinned-dispatcher"), "static-file-router")

  val routes = Routes({
    case HttpRequest(request) => request match {
      case GET(Path("/")) =>
        // Redirect to index.html
        // This is a quick non-blocking operation so executing it in the netty thread pool is OK. 
        request.response.redirect("/index.html")

      case GET(Path(fileName)) =>
        // log.debug(new File(contentDir, fileName).getAbsolutePath)
        // log.debug(new File(contentDir.getAbsolutePath+"/"+ fileName).getAbsolutePath)
        log.debug("web" + fileName)
        staticContentHandlerRouter ! new StaticResourceRequest(request, "web" + fileName)

    }

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/roverSocket") =>
        // To start Web Socket processing, we first have to authorize the handshake.
        // This is a security measure to make sure that web sockets can only be established at your specified end points.
        wsHandshake.authorize()

    }

    case WebSocketFrame(wsFrame) =>
      // Once handshaking has taken place, we can now process frames sent from the client
      //actorSystem.actorOf(Props(classOf[RoverHandler])) ! wsFrame
      roverHandler ! wsFrame
  })

  val webServer = new WebServer(new WebServerConfig(ConfigFactory.load(), "webserver"), routes, actorSystem)

  //
  // STEP #3 - Start and Stop Socko Web Server
  //
  def main(args: Array[String]) {
    // Start web server
    //    val webServer = new WebServer(WebServerConfig(), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run() = {
        webServer.stop()
        // contentDir.delete()
        tempDir.delete()
      }
    })
    webServer.start()

    //System.out.println("Open your browser and navigate to http://localhost:8888")
  }

  private def createTempDir(namePrefix: String): File = {
    val d = File.createTempFile(namePrefix, "")
    d.delete()
    d.mkdir()
    d
  }

  private def deleteTempDir(dir: File) {
    if (dir.exists()) {
      val files = dir.listFiles()
      files.foreach(f => {
        if (f.isFile) {
          f.delete()
        } else {
          deleteTempDir(dir)
        }
      })
    }
    dir.delete()
  }
}

