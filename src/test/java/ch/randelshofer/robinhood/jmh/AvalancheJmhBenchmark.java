package ch.randelshofer.robinhood.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                    Mode  Cnt  Score   Error  Units
 * GoldenRatioAvalancheInlined  avgt   25  1.900 ± 0.004  ns/op
 * Murmur3AvalancheInlined      avgt   25  2.515 ± 0.004  ns/op
 * </pre>
 */
public class AvalancheJmhBenchmark {
    private static int x = (int) System.currentTimeMillis();

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public int measureGoldenRatioAvalancheInlined() {
        int x1 = x;
        int h = x1 * 0x9E3779B9;
        return h ^ (h >>> 16);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public int measureMurmur3AvalancheInlined() {
        int h = x;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }
}
