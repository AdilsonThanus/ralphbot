package com.adilsonthanus.ralphbot

import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.WebServer
import org.mashupbots.socko.webserver.WebServerConfig
import akka.actor.ActorSystem
import akka.actor.Props
import com.adilsonthanus.ralphbot.web.RoverHandler
import java.io.File
import org.mashupbots.socko.handlers.StaticContentHandlerConfig
import com.typesafe.config.ConfigFactory
import org.mashupbots.socko.handlers.StaticContentHandler
import com.adilsonthanus.ralphbot.web.RoverHandler
import akka.routing.FromConfig
import org.mashupbots.socko.handlers.StaticFileRequest
import com.adilsonthanus.ralphbot.core.Board
import com.adilsonthanus.ralphbot.core.OpenBoard

object RalphBotApp extends Logger {

  val contentDir = new File("web/app")
  val tempDir = createTempDir("temp_")
  val staticContentHandlerConfig = StaticContentHandlerConfig(
    rootFilePaths = Seq(contentDir.getAbsolutePath),
    tempDir = tempDir)

  //
  // STEP #1 - Define Actors and Start Akka
  //
  // We are going to start StaticContentHandler actor as a router.
  // There will be 5 instances, each instance having its own thread since there is a lot of blocking IO.
  //
  // FileUploadHandler will also be started as a router with a PinnedDispatcher since it involves IO.
  //
  val actorConfig = """
	my-pinned-dispatcher {
	  type=PinnedDispatcher
	  executor=thread-pool-executor
	}
	akka {
	  event-handlers = ["akka.event.slf4j.Slf4jEventHandler"]
	  loglevel=DEBUG
	  actor {
	    deployment {
	      /static-file-router {
	        router = round-robin
	        nr-of-instances = 5
	      }
	      /roverBoard {
	        router = round-robin
	        nr-of-instances = 1
	      }
	    }
	  }
	}"""

  val actorSystem = ActorSystem("RalphBotActorSystem", ConfigFactory.parseString(actorConfig))

  val board = actorSystem.actorOf(Props(classOf[Board]).
      withRouter(FromConfig()).withDispatcher("my-pinned-dispatcher"), "roverBoard")

  log.info("iniciando rover")
  board ! OpenBoard

  val staticContentHandlerRouter = actorSystem.actorOf(Props(new StaticContentHandler(staticContentHandlerConfig))
    .withRouter(FromConfig()).withDispatcher("my-pinned-dispatcher"), "static-file-router")

  //
  // STEP #2 - Define Routes
  //
  val routes = Routes({
    case HttpRequest(request) => request match {
      case GET(Path("/")) => {
        // Redirect to index.html
        // This is a quick non-blocking operation so executing it in the netty thread pool is OK. 
        request.response.redirect("http://localhost:8888/html/index.html")
      }
      case GET(Path(fileName)) => {
        // Download requested file
        log.debug(fileName)
        staticContentHandlerRouter ! new StaticFileRequest(request, new File(contentDir, fileName))
      }
    }

    case WebSocketHandshake(wsHandshake) => wsHandshake match {
      case Path("/roverSocket") => {
        // To start Web Socket processing, we first have to authorize the handshake.
        // This is a security measure to make sure that web sockets can only be established at your specified end points.
        wsHandshake.authorize()
      }
    }

    case WebSocketFrame(wsFrame) => {
      // Once handshaking has taken place, we can now process frames sent from the client
      actorSystem.actorOf(Props[RoverHandler]) ! wsFrame
    }
  })

  val webServer = new WebServer(WebServerConfig(), routes, actorSystem)
  //
  // STEP #3 - Start and Stop Socko Web Server
  //
  def main(args: Array[String]) {
    // Start web server
//    val webServer = new WebServer(WebServerConfig(), routes, actorSystem)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run {
        webServer.stop()
        contentDir.delete()
        tempDir.delete()
      }
    })
    webServer.start()

    System.out.println("Open your browser and navigate to http://localhost:8888")
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

