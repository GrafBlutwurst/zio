package zio.stm

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

import zio._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class TMapOpsBenchmarks {
  import IOBenchmarks.unsafeRun

  @Param(Array("0", "10", "100", "1000", "10000", "100000"))
  var size: Int = _

  private var idx: Int            = _
  private var map: TMap[Int, Int] = _

  // used to ammortize the relative cost of unsafeRun
  // compared to benchmarked operations
  private val calls = (0 to 500).toList

  @Setup(Level.Trial)
  def setup(): Unit = {
    val data = (1 to size).toList.zipWithIndex

    idx = size / 2
    map = unsafeRun(TMap.fromIterable(data).commit)
  }

  @Benchmark
  def lookup(): Unit =
    unsafeRun(ZIO.foreach_(calls)(_ => map.get(idx).commit))

  @Benchmark
  def update(): Unit =
    unsafeRun(ZIO.foreach_(calls)(_ => map.put(idx, idx).commit))

  @Benchmark
  def transform(): Unit =
    unsafeRun(map.transform((k, v) => (k, v)).commit)

  @Benchmark
  def transformM(): Unit =
    unsafeRun(map.transformM((k, v) => STM.succeedNow(v).map(k -> _)).commit)

  @Benchmark
  def removal(): Unit =
    unsafeRun(ZIO.foreach_(calls)(_ => map.delete(idx).commit))
}
