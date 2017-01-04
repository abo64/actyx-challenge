package abo.actyx.challenge2

import scala.concurrent.Future
import scala.language.postfixOps

import abo.actyx.api.MachineType
import abo.actyx.config.ActyxConfig
import abo.actyx.stream.Sources
import abo.machinelearning.DataSet
import abo.machinelearning.MLRegression
import abo.machinelearning.MLResult
import abo.machinelearning.Weight
import abo.util.ConsoleIO
import abo.util.LineIterator
import abo.util.Maps.MapOps
import abo.util.Seqs.SeqOps
import abo.util.Tabulator
import akka.stream.IOResult
import akka.stream.scaladsl.Source
import akka.util.ByteString

// correlation between the three environmental factors temperature, pressure and humidity,
// and the current drawn by the different types of machines
// (inferred types added for better readability)
object EnvironmentalCorrelationAnalysisBatchApp extends App with ConsoleIO {
/*
  // TODO remove code duplication
  val machineParkApi: MachineParkApi = MachineParkApiImpl

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
*/

  val mLRegression = MLRegression.getImpl
  def learn(dataSet: DataSet): MLResult =
    mLRegression.learn(dataSet)

  val csvLines: Iterator[String] = {
    banner(s"fetching data from file '${ActyxConfig.MLDataFile}'")
    LineIterator.fromFile(ActyxConfig.MLDataFile)
  }

  val dsPerType: Map[MachineType, DataSet] =
    csvLines
      .map(TrainingDataCsvCodec.decode)
      .toSeq
      .groupBy(_._1)
      .strictMapValues(_.map(_._2))
      .strictMapValues(MLConversions.toDataSet)

  val learnings: Map[MachineType, MLResult] =
    dsPerType
      .strictMapValues(learn)

  private def asTable(learnings: Map[MachineType, MLResult]): String = {
    def weightList(weight: Weight): List[String] =
      weight map (_.toString) toList
    def resultList(mlResult: MLResult): List[String] =
      mlResult fold (e => List(s"Error: $e", "", ""), weightList)
    val headers = List("machine type", "pressure", "temperature", "humidity")
    val machineTypes = learnings.keys toList
    val results = learnings.values.toList map resultList
    val rows = (machineTypes zipWith results) {
      case ((machineType, result)) => machineType :: result
    } toList
    val table = headers :: rows

    Tabulator.format(table)
  }

  println(asTable(learnings))

  val trainingDataPerType: Source[TrainingDataPerType, Future[IOResult]] = {
    val byteStringToCsvLines: ByteString => Seq[CsvLine] =
      _.decodeString("UTF-8").split('\n')
    Sources.fromPath(ActyxConfig.MLDataFile.toPath, byteStringToCsvLines andThen (_.map(TrainingDataCsvCodec.decode)))
  }

//  val dataSetsPerType: Source[Map[String, DataSet], Cancellable] = {
//    def mergeTrainingData(tdPerType: TrainingDataPerType): Map[MachineType, Seq[TrainingData]] =
//      tdPerType groupBy (_._1) mapValues (_.map(_._2))

//  val examplesPerType: Source[(MachineType, Example), _] =
//    trainingDataPerType
//      .map { case ((mt, td)) => (mt, MLConversions.toExample(td)) }

//    .to(Sink.seq)
//      .map(mergeTrainingData)
//      .map(_.mapValues(MLConversions.toDataSet))
//      .map (_.kestrel { s => banner(s"$s") })

//  val (sink, trainingDataPerTypeFuture) = Sinks.toSeq[TrainingDataPerType]

//  val dataSetsPerTypeFuture: Future[Map[MachineType, DataSet]] =
//    trainingDataPerTypeFuture
//      .map(_.groupBy(_._1))
//      .map(forcedMapValues(_)(_.map(_._2)))
//      .map(forcedMapValues(_)(MLConversions.toDataSet))

  val dataSetsPerType: Source[Map[MachineType, DataSet], Future[IOResult]] =
    trainingDataPerType
      .map(_.groupBy(_._1))
      .map(_.strictMapValues(_.map(_._2)))
      .map(_.strictMapValues(MLConversions.toDataSet))

  val learningsPerType: Source[Map[String, MLResult], Future[IOResult]] =
    dataSetsPerType
      .map(_.strictMapValues(learn))

//  val ioResultFuture: Future[IOResult] =
//    trainingDataPerType
//      .map(_.kestrel(d => println(s"${LocalDateTime.now} ${d.size}")))
//      .to(Sink.foreach(println))
//      .run
//
//
//   val ioResult = Await.result(ioResultFuture, 10 seconds)
//   if (ioResult.status.isFailure)
//     println(ioResult.status.failed.get)
//   else
//     println("success")

//  addCancellable(cancellable, "learningsPerType")

//  val learnings: Map[String, MLResult] =
//    Await.result(learningsPerType, 10 seconds)
//  banner(learnings.toString)

//  waitForReturnKeyPressed
//  shutdown
}
