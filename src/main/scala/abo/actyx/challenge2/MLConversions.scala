package abo.actyx.challenge2

import scala.language.postfixOps
import scala.language.implicitConversions

import abo.machinelearning.DataSet
import abo.machinelearning.Example

object MLConversions {

  private implicit def tuple3ToVector[T](t3: (T, T, T)): Vector[T] =
    t3.productIterator.toVector map (_.asInstanceOf[T])

  def toExample(trainingData: TrainingData): Example = {
    (trainingData._1, trainingData._2)
  }

  def toDataSet(tdIterable: Iterable[TrainingData]): DataSet =
    tdIterable map toExample toSet
}
