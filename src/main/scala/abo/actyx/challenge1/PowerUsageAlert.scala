package abo.actyx.challenge1

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import abo.actyx.api.Machine
import abo.actyx.api.MachineId
import abo.actyx.api.MachineIds
import abo.actyx.api.MachineParkApi
import abo.actyx.config.ActyxConfig
import abo.actyx.stream.Flows
import abo.actyx.stream.Sources.SourceOps
import abo.akka.AkkaSystem
import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

// (inferred types added for better readability)
trait PowerUsageAlert extends AkkaSystem {

  def machineParkApi: MachineParkApi

  type MachineWithAverage = (Machine, Flows.Average)

  def alertOnThresholdExcess[T](averageWindowSize: Int)(
      machineSource: Source[Machine, T]): Source[Machine, T] =
  {
    def exceedsAlertThreshold(machineWithAverage: MachineWithAverage): Boolean = {
      val machine = machineWithAverage._1
      machine.current >= machine.currentAlert
    }

    machineSource
//      .map (_.kestrel {m => println(s"$counter: $m"); counter += 1 })
      .log("machine-data", identity)
      .via(Flows.movingAverage(averageWindowSize, _.current))
//      .sliding(averageWindowSize, 1)
//      .map (_.kestrel {w => println(s"***window $w") })
      .filter(exceedsAlertThreshold)
//      .map(_.kestrel(onAlert.tupled))
      .via(PowerUsageAlerts.consoleAlert)
      .map(_._1)
//      .via(Flows.movingAverage(_.current))
//      .map (_.kestrel {m => println(s"***should warn $m") })
//      .map(machineWithAverageCurrent)
//      .map {m => onAlert(m); m }
//      .to(Sink.foreach[Machine](onAlert))
  }

//  def consoleAlert(machine: Machine, averageCurrent: Average): Unit = {
//    def flashy(message: String) =
//      s"\n****** ALERT ******\n$message\n*******************\n"
//
//    val now = //LocalDateTime.now
//      ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)
//    val message =
//      s"$now\n$machine\nwith average current for last ${Config.AverageCurrentInterval}: $averageCurrent"
//
//      println(flashy(message))
//  }

  val machineIdsSource: Source[MachineIds, _] = machineParkApi.machineIdsSource
  val machineIdsFuture: Future[MachineIds] =
    machineIdsSource.runFold(Set.empty[MachineId])(_ ++ _)
  val machineIds = Await.result(machineIdsFuture, 10 seconds)
  banner(s"${machineIds.size} machines")

  val machineSources: Set[Source[Machine, NotUsed]] =
    machineIds map machineParkApi.machineSource
//    machineIds map (machineParkApi.machineSource(_, Config.ApiMachineQueryInterval))
//    machineIds map {
//    ((Sources.withTick[Machine](Config.ApiMachineQueryInterval) _).compose (machineParkApi.machineSource(_)))
//  }

//  val tickedMachineSources: Set[Source[Machine, Cancellable]] =
//    machineSources map (_.withTick(Config.ApiMachineQueryInterval))

  val averageWindowSize = (ActyxConfig.AverageCurrentInterval / ActyxConfig.ApiMachineQueryInterval) toInt

  val alertedMachineSources: Set[Source[Machine, _]] =
    machineSources map alertOnThresholdExcess(averageWindowSize)
//    tickedMachineSources map alertOnThresholdExcess(consoleAlert, averageWindowSize)

//  val throttledMachineSources: Set[Source[Machine, _]] =
//    alertedMachineSources map (_.throttle(1, 1 second, 1, ThrottleMode.Shaping))
  
  val mergedMachineSources: Source[Machine, _] =
    alertedMachineSources reduce (_ merge _)

  val tickedMergedSources: Source[Machine, Cancellable] =
    mergedMachineSources.withTick(ActyxConfig.ApiMachineQueryInterval)

  val cancellable = //mergedMachineSources.to(Sink.ignore).run
    tickedMergedSources.to(Sink.ignore).run

  addCancellable(cancellable, "tickedMergedSources")

  waitForReturnKeyPressed
  shutdown
}