package abo.actyx.stream

import java.nio.file.Path

import scala.concurrent.Future
import scala.concurrent.Promise

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.coding.Gzip
import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink
import akka.util.ByteString

object Sinks {
  // TODO make this Flow-based
  def toPath[T, Mat](file: Path, toByteString: T => ByteString): Sink[T, Future[IOResult]] = {
    val compressed = file.toString.toLowerCase.endsWith(".gz")

    val f: T => ByteString =
      if (compressed) (Gzip.encode(_: ByteString)) compose toByteString
      else toByteString

    FileIO.toPath(file)
      .contramap(f)
  }

  def toSeq[T](implicit system: ActorSystem): (Sink[T, NotUsed], Future[Seq[T]]) = {
    val promise: Promise[Seq[T]] = Promise()
    val sink: Sink[T, NotUsed] =
      Sink.seq[T].mapMaterializedValue { ft => promise.completeWith(ft); NotUsed }
    (sink, promise.future)
  }

  def lastValue[T, Mat2](mat2: Mat2 = NotUsed)(
      implicit system: ActorSystem): (Sink[T, Mat2], Future[T]) = {
    val promise: Promise[T] = Promise()
    val sink: Sink[T, Mat2] =
      Sink.last[T].mapMaterializedValue { ft => promise.completeWith(ft); mat2 }
    (sink, promise.future)
  }
}
