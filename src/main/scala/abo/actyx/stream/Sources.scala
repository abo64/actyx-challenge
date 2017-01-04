package abo.actyx.stream

import java.nio.file.Path

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import akka.actor.Cancellable
import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Source
import akka.util.ByteString

object Sources {

  def withTick[T](interval: FiniteDuration)(source: Source[T, _]): Source[T, Cancellable] =
    Source.tick(Duration.Zero, interval, 0) flatMapConcat { _ => source }

  implicit class SourceOps[T, M](source: Source[T, _]) {
    def withTick[T](interval: FiniteDuration) = Sources.withTick(interval)(source)
  }

  def fromPath[T](file: Path, fromByteString: ByteString => T): Source[T, Future[IOResult]] = {
    FileIO.fromPath(file)
      .map(fromByteString)
  }
}
