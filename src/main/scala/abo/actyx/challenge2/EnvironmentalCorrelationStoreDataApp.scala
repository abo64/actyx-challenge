package abo.actyx.challenge2

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import abo.actyx.api.Machine
import abo.actyx.api.MachineId
import abo.actyx.api.MachineIds
import abo.actyx.api.MachineParkApi
import abo.actyx.api.MachineParkApiImpl
import abo.actyx.api.MachineType
import abo.actyx.api.Sensor
import abo.actyx.config.{ ActyxConfig => Config }
import abo.actyx.stream.Sinks
import abo.actyx.stream.Sources.SourceOps
import abo.akka.AkkaSystem
import abo.util.Kestrel.KestrelOps
import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString

// gather data for ML periodically and store them in a file
// for later batch processing
object EnvironmentalCorrelationStoreDataApp  extends App with AkkaSystem {

  // TODO remove code duplication
  val machineParkApi: MachineParkApi = new MachineParkApiImpl

  val machineIdsSource: Source[MachineIds, _] = machineParkApi.machineIdsSource
  val machineIdsFuture: Future[MachineIds] =
    machineIdsSource.runFold(Set.empty[MachineId])(_ ++ _)
  val machineIds = Await.result(machineIdsFuture, 10 seconds)
  banner(s"${machineIds.size} machines")

  val machineSources: Set[Source[Machine, NotUsed]] =
    machineIds map machineParkApi.machineSource

  val machinesSource: Source[Set[Machine], NotUsed] =
    // TODO move to Sources as Sequence or so
    machineSources.foldLeft(Source.single(Set.empty[Machine])) {
      case (sources, machineSource) =>
        sources flatMapConcat { machines => machineSource map (m => machines + m) }
    }

  val sensorSource: Source[Sensor, NotUsed] =
    machineParkApi.sensorSource
      .log("sensor-data", identity)

  val trainingDataPerType: Source[TrainingDataPerType, NotUsed] =
    sensorSource.zipWith(machinesSource) {
      case (s, machines) =>
        def toTrainingData(m: Machine): MachineTypeTrainingData =
          m.`type` -> ((s.pressure, s.temperature, s.humidity) -> m.current)

        machines.toSeq
          .map(toTrainingData)
    }

  val tickedTrainingDataPerType: Source[TrainingDataPerType, Cancellable] =
    trainingDataPerType.withTick(Config.ApiSensorQueryInterval)

  val tickedTrainingDataToFile: Source[TrainingDataPerType, Cancellable] = {
    def trainingDataToCsv(trainingDataPerType: TrainingDataPerType): ByteString = {
      val csvLines = trainingDataPerType map TrainingDataCsvCodec.encode
      ByteString(csvLines.mkString("", "\n", "\n"))
    }

    val file = Config.MLDataFile

    tickedTrainingDataPerType
      .map (_.kestrel { s => banner(s"writing ${s.size} training data to file '${file.getName}'") })
      .alsoTo(Sinks.toPath(file.toPath, trainingDataToCsv))
  }

  val cancellable =
    tickedTrainingDataToFile
      .to(Sink.foreach(println))
      .run

  addCancellable(cancellable, "tickedTrainingDataToFile")

  waitForReturnKeyPressed
  shutdown
}
