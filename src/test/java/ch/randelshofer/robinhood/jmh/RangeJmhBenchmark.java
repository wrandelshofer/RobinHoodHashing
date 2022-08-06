package ch.randelshofer.robinhood.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

/**
 <pre>
 # JMH version: 1.28
 # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz

 Benchmark             Mode  Cnt  Score   Error  Units
 PowerOf2RangeInlined  avgt   25  2.071 ± 0.005  ns/op
 FastRangeInlined      avgt   25  2.358 ± 0.011  ns/op
 FastModInlined        avgt   25  2.460 ± 0.152  ns/op
 FloorModRangeInlined  avgt   25  3.492 ± 0.021  ns/op
 ModuloRangeInlined    avgt   25  3.572 ± 0.010  ns/op
 </pre>
 */
public class RangeJmhBenchmark {
    private static int word = (int) System.currentTimeMillis();
    private static int p = Math.max(word & 65535, 3);
    private static long invp = Long.divideUnsigned(-1L, p) + 1;
    private static int powerOf2 = Integer.highestOneBit(p + (p - 1));

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public int measureFastRangeInlined() {
        return (int) (((0xffffffffL & word) * p) >>> 32);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public int measureFastModInlined() {
        long lowbits = invp * word;
        int mod = (int) Math.multiplyHigh(lowbits, p);
        return mod < 0 ? p + mod : mod;
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public int measureFloorModRangeInlined() {
        return Math.floorMod(word, p);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public int measureModuloRangeInlined() {
        return Math.abs(word % p);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public int measurePowerOf2RangeInlined() {
        return word & (powerOf2 - 1);
    }

}
