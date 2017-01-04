package abo.util

object Maps {

  def strictMapValues[A, B, C](m: Map[A, B])(f: B => C): Map[A, C] =
    m.map { case ((a, b)) => a -> f(b) }

  implicit class MapOps[A, B](m: Map[A, B]) {
    def strictMapValues[C](f: B => C): Map[A, C] =
      Maps.strictMapValues(m)(f)
  }
}
