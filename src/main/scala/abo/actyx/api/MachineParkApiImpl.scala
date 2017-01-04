package abo.actyx.api

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import abo.actyx.api.json.MachineParser
import abo.actyx.api.json.SensorParser
import abo.actyx.config.{ ActyxConfig => Config }
import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.javadsl.model.Uri
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.Uri.apply
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.util.ByteString

class MachineParkApiImpl(implicit system: ActorSystem, materializer: ActorMaterializer)
  extends MachineParkApi
{
  val log = Logging(system, this.getClass)

  import system.dispatcher

  override def machineIdsSource: Source[MachineIds, NotUsed] = {
    def httpResponseToFutureString(httpResponse: HttpResponse): Future[String]=
      httpResponse.entity.toStrict(Config.ApiTimeout).map (_.data.decodeString("UTF-8"))

    def parseIds(jsonIds: String): MachineIds = {
      // TODO parse with spray-json
      jsonIds.replaceAll("\\[", "").replaceAll("\\]", "").split(',')
        .map(MachineParkApiImpl.getId)
        .toSet
    }

    val machinesUrl = s"${Config.ApiUrl}/machines"

    httpStringSource(machinesUrl, continueWithWarning(status => s"$machinesUrl: Error $status while fetching machine ids")) map parseIds
  }

  private val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = {
    val host = Uri.create(Config.ApiUrl).host
    Http().outgoingConnection(host = host.address)
  }

  private def responseSource(uri: String): Source[HttpResponse, NotUsed] =
    Source.single(HttpRequest(uri = uri)).via(connectionFlow)

  private def responseToStringSource(httpResponse: HttpResponse): Source[String, NotUsed] =
    Source.single(httpResponse)
      .mapAsync(1)(_.entity.dataBytes.runFold(ByteString(""))(_ ++ _))
      .map(_.decodeString("UTF-8"))

  private def httpStringSource(uri: String,
      onError: StatusCode => Source[String, NotUsed]): Source[String, NotUsed] =
    // TODO use recover/recoverWith
    responseSource(uri) flatMapConcat { httpResponse =>
      val status = httpResponse.status
      if (status.isFailure) {
        httpResponse.discardEntityBytes(materializer)
        onError(status)
      } else responseToStringSource(httpResponse)
  }

  override def machineSource(id: MachineId): Source[Machine, NotUsed] = {
    val machineUrl = s"${Config.ApiUrl}/machine/$id"

    httpStringSource(machineUrl,
          continueWithWarning(status => s"$machineUrl: $status while fetching machine '$id'"))
      .map(MachineParser.parse(id))
  }

  private def continueWithWarning[T](message: StatusCode => String)(status: StatusCode): Source[T, NotUsed] = {
    log.warning(s"${message(status)}. Continuing ...")
    Source.empty
  }

  override def sensorSource: Source[Sensor, NotUsed] = {
    val sensorUrl = s"${Config.ApiUrl}/env-sensor"

    httpStringSource(sensorUrl, continueWithWarning(status => s"$sensorUrl: $status while fetching sensor"))
      .map(SensorParser.parse)
  }
}

object MachineParkApiImpl {

  private val machineRegex = """\"\$API_ROOT\/machine\/([^"]+)\"""".r

  private[api] def getId(machineDescription: String): MachineId =
    machineDescription match {
      case machineRegex(id) => id
  }

}