package abo.machinelearning

import org.scalatest.FlatSpec
import org.scalacheck.Gen
import scala.annotation.tailrec
import scala.util.Random

class MultipleLinearRegressionSpec extends FlatSpec {

  behavior of "learn"

  it should "converge for multiple regression" in {
    val dim = 3
    val ones = Vector.fill(dim)(1d)
    val weight = Vector.tabulate(dim)(_ * 4d)
    val f: TargetFunction = weight dot _
    val dataSet = TestDataGen.dataSet(dim, f, 1000)

    val result =
      MultipleLinearRegression.learn(dataSet).right.get

//    println(result)
    assert(math.abs((result dot ones) - (weight dot ones)) <= 0.1)
  }
}
