package com.dwolla

object RandomChunks {
  import _root_.fs2._

  def apply[F[_], T](maxChunkSize: Int): Pipe[F, T, T] = {
    val r = new scala.util.Random()

    def pull(): Stream[F, T] => Pull[F, T, Unit] =
      _.pull.unconsN(maxChunkSize, allowFewer = true).flatMap {
        case None => Pull.done
        case Some((c: Chunk[T], rest: Stream[F, T])) =>
          if (c.size < 2) Pull.output(c)
          else {
            val (a, b) = c.splitAt(r.nextInt(c.size + 1))

            Pull.output(a) >> pull()(Stream.chunk(b) ++ rest)
          }
      }

    pull()(_).stream
  }
}
