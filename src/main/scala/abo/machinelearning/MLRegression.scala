package abo.machinelearning

trait MLRegression {
  /**
   * Calculate the correlation based on dataSet.
   * MLResult is either the learned correlation for the input or an error message
   */
  def learn(dataSet: DataSet): MLResult
}

object MLRegression {
  val getImpl: MLRegression =
//    GaussianProcess
    OrdinaryLeastSquares
//    MultipleLinearRegression
}
