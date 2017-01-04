package abo.actyx.stream

import akka.NotUsed
import akka.stream.scaladsl.Flow

object Flows {
  type Average = Double

  // TODO use min of total T's so far (w/ scan) and windowSize, return min in triple
  def movingAverage[T](windowSize: Int,
      whichValue: T => Double): Flow[T, (T, Average), NotUsed] =
  {
    def withAverage(ts: Seq[T]): (T, Average) =
      (ts.last, ts.map(whichValue).sum / ts.size)

    Flow[T]
      .sliding(windowSize, 1)
      .map(withAverage)
  }
}
