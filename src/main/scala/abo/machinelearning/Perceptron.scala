package abo.machinelearning

import scala.annotation.tailrec
import scala.util.Random
import abo.util.Kestrel._

// just for fun b/c I had it implemented in the past already
// problem is that it only converges for binary classification,
// cf respective test PerceptronSpec
object Perceptron extends IncrementalML {

  // see https://en.wikipedia.org/wiki/Perceptron#Learning_algorithm
  override def learn(oldWeight: Weight, dataSet: DataSet): MLResult = {
    val dim: Dimension = oldWeight.length

    val h: Hypothesis = (w, x) => w dot x

    def update(w: Weight, example: Example): Weight = {
      val (x, y) = example

      Vector.tabulate(dim) { i =>
        w(i) + (y - h(w, x)) * x(i)
      }
    }

    val result =
      dataSet.foldLeft(oldWeight) { case (weight, example) =>
        update(weight, example) //.kestrel(println)
      }
    Right(result)
  }

  override def startWeight(dimension: Dimension): Weight =
    Vector.fill(dimension + 0)(0d)
}
