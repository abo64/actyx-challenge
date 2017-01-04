package abo.actyx.api

import scala.language.postfixOps

import akka.NotUsed
import akka.stream.scaladsl.Source

// to test locally without any http requests
object MachineParkApiTestImpl extends MachineParkApi {

  override def machineIdsSource: Source[MachineIds, NotUsed] =
    Source(Set((1 to 10) map (_.toString) toSet))

  override def machineSource(id: MachineId): Source[Machine, NotUsed] = {
    Source.cycle { () =>
      Iterator.continually(Gens.arbitraryMachine(id).arbitrary.sample.get)
    }
  }

  override def sensorSource: Source[Sensor, NotUsed] = ???

}