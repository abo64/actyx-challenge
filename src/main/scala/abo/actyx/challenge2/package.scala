package abo.actyx

import abo.actyx.api.Current
import abo.actyx.api.Humidity
import abo.actyx.api.MachineType
import abo.actyx.api.Pressure
import abo.actyx.api.Temperature

package object challenge2 {
  type TrainingData = ((Pressure, Temperature, Humidity), Current)
  type MachineTypeTrainingData = (MachineType, TrainingData)
  type TrainingDataPerType = Seq[MachineTypeTrainingData]
  type CsvLine = String
}
