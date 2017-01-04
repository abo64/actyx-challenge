package abo.machinelearning

trait IncrementalML {
  /**
   * Calculate the new Weight based on dataSet.
   * MLResult is either the learned weights for the input or an error message
   */
  def learn(oldWeight: Weight, dataSet: DataSet): MLResult

  def startWeight(dimension: Dimension): Weight
}

object IncrementalML {
  // TODO find a good library that supports this
  // for example implementations of smile.regression.OnlineRegression
  // or org.apache.commons.math4.stat.regression.UpdatingMultipleLinearRegression
  // once math4 is available
  val getImpl: IncrementalML =
    throw new Exception("TODO: find implementations for IncrementalML")
}