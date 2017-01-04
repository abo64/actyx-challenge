package abo.actyx.api

import scala.concurrent.duration.FiniteDuration

import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.scaladsl.Source

trait MachineParkApi {

  def machineIdsSource: Source[MachineIds, NotUsed]

  def machineSource(id: MachineId): Source[Machine, NotUsed]

  def sensorSource: Source[Sensor, NotUsed]
}