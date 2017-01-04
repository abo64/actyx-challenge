package abo.util

trait ConsoleIO {

  def banner(message: String): Unit =
    println(s"*** $message ***")

  def waitForReturnKeyPressed: Unit = {
    banner("Press <return> to cancel")
    scala.io.StdIn.readLine
  }
}
