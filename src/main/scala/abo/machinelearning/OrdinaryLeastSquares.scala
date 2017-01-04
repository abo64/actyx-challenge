package abo.machinelearning

import scala.language.postfixOps

import smile.regression.ols

object OrdinaryLeastSquares extends MLRegression {
  override def learn(dataSet: DataSet): MLResult = {
    val (x, y) = sampleData(dataSet)

    val result: MLResult =
      try {
        val model = ols(x , y)
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
