package abo.machinelearning

import scala.language.postfixOps

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression

object MultipleLinearRegression extends MLRegression {
  override def learn(dataSet: DataSet): MLResult = {
    val (x, y) = sampleData(dataSet)

    val result: MLResult =
      try {
        val ols = new OLSMultipleLinearRegression
        ols.newSampleData(y, x)
        val regressionParameters = ols.estimateRegressionParameters
        val mlResult = regressionParameters.tail map round(2) toVector

        Right(mlResult)
      } catch {
        case e: Exception => Left(e.getMessage)
      }

    result
  }
}