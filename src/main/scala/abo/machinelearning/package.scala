package abo

import scala.language.implicitConversions
import scala.language.postfixOps

// some implicit magic
// at least they are package-private
package object machinelearning {

  // some terminology taken from the book
  // "Learning from Data" http://work.caltech.edu/textbook.html
  type Dimension = Int
  type Input = Vector[Double]
  type Output = Double
  type TargetFunction = Input => Output
  type Example = (Input, Output)
  type DataSet = Set[Example]
  type DataSetSize = Int
  type Weight = Vector[Double]
  type MLResult = Either[String, Weight]
  type Hypothesis = (Weight, Input) => Output
  type Matrix = Vector[Vector[Double]]

  private[machinelearning] def sampleData(dataSet: DataSet): (Matrix, Vector[Output]) = {
      val dataSeq = dataSet toSeq
      val y: Vector[Output] = dataSeq map (_._2) toVector
      val x: Matrix = dataSeq map (_._1) toVector

      (x, y)
    }

  private[machinelearning] def round(decimals: Int)(d: Double): Double = {
    val factor = math.pow(10, decimals)
    Math.round(d * factor) / factor
  }

  private[machinelearning] implicit def vectorToArray(v: Vector[Double]): Array[Double] =
    v.toArray

  private[machinelearning] implicit def matrixToArray2(m: Matrix): Array[Array[Double]] =
    m map (_.toArray) toArray

  private[machinelearning] implicit class VectorOps(self: Vector[Double]) {
    def dim: Dimension = self.length

    def *(other: Vector[Double]): Vector[Double] = {
      val dim = self.dim
      require(dim == other.dim, "Vectors must be of same dimension")

      Vector.tabulate(dim) { i => self(i) * other(i) }
    }

    def dot(other: Vector[Double]): Double =
      (self * other) sum
  }

//  private[machinelearning] implicit class DoubleOps(self: Double) {
//    def *(vector: Vector[Double]): Vector[Double] =
//      vector map { case vi => self * vi }
//  }
}