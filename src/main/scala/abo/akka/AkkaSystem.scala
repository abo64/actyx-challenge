package abo.akka

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import abo.util.ConsoleIO
import akka.actor.ActorSystem
import akka.actor.Cancellable
import akka.stream.ActorMaterializer

trait AkkaSystem extends ConsoleIO {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executor = system.dispatcher

  val log = system.log

  private val cancellables = mutable.Set.empty[(String, Cancellable)]

  def addCancellable(cancellable: Cancellable, name: String) =
    cancellables.add(name -> cancellable)

  def shutdown =  {
    if (cancellables.nonEmpty) {
      cancellables foreach { case ((name, cancellable)) =>
        banner(s"Cancelling '$name'")
        cancellable.cancel // this does not really cancel all underlying Sources. Is there a better way?
      }
    }

    banner(s"Terminating ActorSystem '${system.name}'")
    val whenTerminated = system.terminate
    Await.result(whenTerminated, Duration.Inf)
    System.exit(0)
  }
}