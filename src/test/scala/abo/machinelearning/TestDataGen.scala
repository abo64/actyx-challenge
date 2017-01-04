package abo.machinelearning

import scala.annotation.tailrec
import scala.util.Random
//import org.scalacheck.Gen
//import org.scalacheck.Arbitrary

object TestDataGen {

  private val random = Random.self

  def dataSet(dim: Dimension, f: TargetFunction, howMany: Int): DataSet = {

    // not uniformly distributed; no time to fix it
//    val dataSetGen: Gen[DataSet] =
//      for {
//        input <- Gen.listOfN(dim, Arbitrary.arbDouble.arbitrary) map (_.toVector)
//        example = (input, f(input))
//        dataSet <- Gen.containerOfN[Set, Example](dim, example)
//      } yield dataSet

    def genInput: Input = (Vector.fill(dim)(random.nextDouble))
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
