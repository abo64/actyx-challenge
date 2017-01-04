package abo.actyx.stream

import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures

import Flows.Average
import Flows.movingAverage
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Source

class FlowsSpec extends FlatSpec with ScalaFutures {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  behavior of "movingAverage"

  it should "calculate the average" in {
    val expectedAverage = {
      val seq = (4 to 6)
      seq.sum / seq.size.toDouble
    }
    val source: Source[Int, NotUsed] = Source(1 to 6)
    val averageFlow: Flow[Int, (Int, Average), NotUsed]= movingAverage[Int](3, _.toDouble)
    val averageSource: Source[(Int, Average), NotUsed] = source.via(averageFlow)
    val (sink, averageFuture) = Sinks.lastValue[(Int, Average), NotUsed]()
    val runnableGraph: RunnableGraph[NotUsed] = averageSource.to(sink)
    runnableGraph.run

    whenReady(averageFuture) { average =>
      assert(average == (6, expectedAverage))
    }
  }
}
