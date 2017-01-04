package abo.actyx.challenge2

import scala.language.postfixOps

trait Codec[A, B] {
  def encode: A => B
  def decode: B => A
}

object TrainingDataCsvCodec extends Codec[MachineTypeTrainingData, CsvLine] {

  override val encode: MachineTypeTrainingData => CsvLine = {
      case ((machineType, ((pressure, temperature, humidity), current))) =>
        s"$machineType,$pressure,$temperature,$humidity,$current"
    }

  override val decode: CsvLine => MachineTypeTrainingData =
    _ split (',') toList match {
      case machineType :: pressure :: temperature :: humidity :: current :: _ =>
        (machineType,
          ((pressure.toDouble, temperature.toDouble, humidity.toDouble), current.toDouble))
      case invalid => throw new Exception(s"invalid csv data: $invalid")
    }
}
