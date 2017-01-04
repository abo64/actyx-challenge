package abo.machinelearning

import scala.language.postfixOps

import smile.math.kernel.GaussianKernel
import smile.regression.gpr

object GaussianProcess extends MLRegression {
  override def learn(dataSet: DataSet): MLResult = {
    val (x, y) = sampleData(dataSet)

    val result: MLResult =
      try {
        val model = gpr[Array[Double]](x , y, new GaussianKernel(3), 0.2)
        val coefficients = model.coefficients
        val mlResult = coefficients map round(2) toVector

//        println(model)

        Right(mlResult)
      } catch {
        case e: Exception => Left(e.getMessage)
      }

    result
  }
}
