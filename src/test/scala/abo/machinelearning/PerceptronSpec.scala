package abo.machinelearning

import org.scalatest.Ignore
import scala.annotation.tailrec
import org.scalatest.FlatSpec
import scala.util.Random

@Ignore
class PerceptronSpec extends FlatSpec {

  it should "converge for multiple regression" in {
    val dim = 3
    val startWeight = Perceptron.startWeight(dim)
    val ones = Vector.fill(dim + 1)(1d)
    val weight = Vector.tabulate(dim + 1)(_ * 4d)
    val f: TargetFunction = weight dot _
    val dataSet = genDataSet(dim, f, 1000)

    val result = Perceptron.learn(startWeight, dataSet).right.get
    println(result)
    assert((result dot ones) - (weight dot ones) <= 1)
  }

  private val random = Random.self

  private def genDataSet(dim: Dimension, f: TargetFunction, howMany: Int): DataSet = {
    def genInput: Input = (Vector.fill(dim)(random.nextDouble)) .+: (1d)

    def genExample: Example = {
      val input = genInput
      (input, f(input))
    }

    @tailrec
    def loop(result: DataSet): DataSet =
      if (result.size == howMany) result
      else loop(result + genExample)

    loop(Set())
  }
}
