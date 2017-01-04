package abo.actyx.challenge1

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

import abo.actyx.api.Machine
import abo.actyx.config.ActyxConfig
import abo.actyx.stream.Flows.Average
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl._
import akka.util.ByteString


object PowerUsageAlerts {
  type MachineWithAverage = (Machine, Average)
  type PowerAlert = Flow[MachineWithAverage, MachineWithAverage, NotUsed]

  val consoleAlert: PowerAlert = {
    def flashy(message: String) =
      s"\n****** ALERT ******\n$message\n*******************\n"

    val consoleAlert: MachineWithAverage => MachineWithAverage = {
      case ((machine: Machine, averageCurrent: Average)) => {
        val now = //LocalDateTime.now
          ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)
        val message =
          s"$now\n$machine\nwith average current for last ${ActyxConfig.AverageCurrentInterval}: $averageCurrent"

        println(flashy(message))
        (machine, averageCurrent)
      }
    }

    Flow.fromFunction(consoleAlert)
  }

  // TODO implement this
  def httpAlert(
    implicit system: ActorSystem, materializer: Materializer,
      executor: ExecutionContext): PowerAlert = {

    val sendAlert: MachineWithAverage => MachineWithAverage = {
      case ((machine: Machine, averageCurrent: Average)) => {
        ???
      }
    }
    val alertFlow = Flow.fromFunction(sendAlert)

    runWebService {
      get {
        path("power-alert") {
          import spray.json.DefaultJsonProtocol._
          import spray.json._
          val outStream = alertFlow
            .map(_._1.id.toJson.prettyPrint)
            .map(ws.TextMessage(_))
//          Flow.fromSinkAndSource(Sink.ignore, Source.fromFuture(outStream.))
//          val flow = Flow[ws.Message]
//            .flatMapConcat (_ => Flow.outStream)
          handleWebSocketMessages(???)
        }
      }
    }
    ???
  }

//  private def runHttpService(handler: Flow[HttpRequest, HttpResponse, Any])(
  private def runWebService(route: Route)(
      implicit system: ActorSystem, materializer: Materializer,
      executor: ExecutionContext): Unit = {
//    val config = system.settings.config.getConfig("app")
//    val interface = config.getString("interface")
//    val port = config.getInt("port")

    val handler = Route.handlerFlow(route)
    val binding = Http().bindAndHandle(handler, "localhost", 8080)

    binding.onComplete {
      case Success(x) ⇒
        println(s"Server is listening on ${x.localAddress.getHostName}:${x.localAddress.getPort}")
      case Failure(e) ⇒
        println(s"Binding failed with ${e.getMessage}")
    }
  }
}
