package abo.actyx.challenge2

import java.time.LocalDateTime

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
import abo.actyx.stream.Sources.SourceOps
import abo.akka.AkkaSystem
import abo.machinelearning.DataSet
import abo.machinelearning.IncrementalML
import abo.machinelearning.MLResult
import abo.util.Kestrel.KestrelOps
import abo.util.Maps.MapOps
import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

// correlation between the three environmental factors temperature, pressure and humidity,
// and the current drawn by the different types of machines.
// requires a ML model that can be incrementally updated with new incoming data
// TODO find such incremental ML libraries
// (inferred types added for better readability)
object EnvironmentalCorrelationAnalysisOnlineApp extends App with AkkaSystem {

  banner("Not implemented yet: Incremental ML model missing")
  shutdown

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


  val incrementalML = IncrementalML.getImpl
  def learn(dataSet: DataSet) =
    incrementalML.learn(incrementalML.startWeight(3), dataSet)

  val dataSetsPerType: Source[Map[String, DataSet], Cancellable] = {
    def mergeTrainingData(tdPerType: TrainingDataPerType): Map[MachineType, Seq[TrainingData]] =
      tdPerType groupBy (_._1) strictMapValues (_.map(_._2))

    tickedTrainingDataPerType
      .map(mergeTrainingData)
      .map(_.strictMapValues(MLConversions.toDataSet))
      .map (_.kestrel { s => banner(s"$s") })
  }

  val learningsPerType: Source[Map[String, MLResult], Cancellable] =
    dataSetsPerType
      .map(_.strictMapValues(learn))

  val cancellable =
    learningsPerType
      .map(_.kestrel(d => println(s"${LocalDateTime.now} ${d.size}")))
      .to(Sink.foreach(println))
      .run

  addCancellable(cancellable, "learningsPerType")

  waitForReturnKeyPressed
  shutdown
}