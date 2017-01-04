package abo.actyx

import java.time.LocalDateTime

package object api {

  type MachineId = String
  type MachineIds = Set[MachineId]

  // simple String will do for now
  type MachineType = String
//  sealed trait MachineType
//  case object Mill extends MachineType
//  case object Lathe extends MachineType
//  case object Saw extends MachineType
//  case object LaserCutter extends MachineType
//  case object `3D-Printer` extends MachineType
//  case object WaterCutter extends MachineType
//  object MachineType {
//    def fromName(name: String): MachineType =
//      name match {
//      case "mill" => Mill
//      case "lathe" => Lathe
//      case "saw" => Saw
//      case "laser-cutter" => LaserCutter
//      case "3d-printer" => `3D-Printer`
//      case "water-cutter" => WaterCutter
//      case unknown => throw new Exception(s"unknown machine type: $unknown")
//    }
//
//    implicit def stringToMachineType(name: String): MachineType = fromName(name)
//  }

  type Current = Double
  type Pressure = Double
  type Temperature = Double
  type Humidity = Double


  sealed trait MachineApiData

  case class Machine(id: MachineId, name: String, timestamp: LocalDateTime, current: Current, state: String,
      location: String, currentAlert: Current, `type`: MachineType) extends MachineApiData

  case class Sensor(pressure: Pressure, temperature: Temperature, humidity: Humidity,
      timestamp: LocalDateTime) extends MachineApiData
}