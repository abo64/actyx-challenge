package abo.util

object Seqs {
  def zipWith[A, B, C](as: Seq[A], bs: Seq[B], f: (A, B) => C): Seq[C] =
    as zip bs map { case ((a, b)) => f(a, b) }

  implicit class SeqOps[A](as: Seq[A]) {
    def zipWith[B, C](bs: Seq[B])(f: (A, B) => C): Seq[C] =
      Seqs.zipWith(as, bs, f)
  }
}
