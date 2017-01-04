package abo.util

// my favorite bird combinator: for side effects
// see https://github.com/raganwald-deprecated/homoiconic/blob/master/2008-10-29/kestrel.markdown#readme
// for a beautiful picture of the bird :)
object Kestrel {
  def kestrel[A](x: A)(f: A => Unit): A = { f(x); x }

  implicit class KestrelOps[A](a: A) {
    def kestrel(f: A => Unit): A = Kestrel.kestrel(a)(f)
  }
}
