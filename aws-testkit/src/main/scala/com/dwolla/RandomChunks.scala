package com.dwolla

import fs2.*

object RandomChunks {
  def apply[F[_], T](maxChunkSize: Int, seed: Long): Pipe[F, T, T] = {
    val random = new scala.util.Random(seed)

    def pull(): Stream[F, T] => Pull[F, T, Unit] =
      _.pull.unconsN(maxChunkSize, allowFewer = true).flatMap {
        case None => Pull.done
        case Some((c: Chunk[T], rest: Stream[F, T])) =>
          if (c.size < 2) Pull.output(c)
          else {
            val (a, b) = c.splitAt(random.nextInt(c.size + 1))
            Pull.output(a) >> pull()(Stream.chunk(b) ++ rest)
          }
      }

    pull()(_).stream
  }
}
