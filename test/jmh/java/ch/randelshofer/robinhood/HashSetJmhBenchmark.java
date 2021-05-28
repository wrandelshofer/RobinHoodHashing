package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark        Mode  Cnt         Score        Error  Units
 * Add              avgt   25  16_39976.379 ±  29641.235  ns/op
 * AddAndGrow       avgt   25  55_09892.521 ± 168496.115  ns/op
 * Clone            avgt   25  26_01282.848 ±  30392.642  ns/op
 * Remove           avgt   25  62_08743.152 ± 336646.110  ns/op
 * SuccessfulGet    avgt   25   9_55005.213 ±  24749.852  ns/op
 * UnsuccessfulGet  avgt   25   6_19470.596 ±   9354.213  ns/op
 * </pre>
 */
public class HashSetJmhBenchmark {

    private static final int[] VALUE_SET = new int[100_000];
    private static final int[] NOT_IN_VALUE_SET = new int[100_000];

    private static final HashSet<Integer> CONSTANT_SET;

    public static final int CAPACITY = 262_144;

    static {
        Random rng = new Random(0);
        HashSet<Integer> set = new HashSet<>(CAPACITY);
        for (int i = 0; i < VALUE_SET.length; ) {
            int v = rng.nextInt(500_000);
            if (set.add(v)) {
                VALUE_SET[i++] = v;
            }
        }
        for (int i = 0; i < VALUE_SET.length; ) {
            int v = rng.nextInt(500_000);
            if (!set.contains(v)) {
                NOT_IN_VALUE_SET[i++] = v;
            }
        }
        CONSTANT_SET = set;
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAdd() {
        HashSet<Integer> set = new HashSet<>(CAPACITY);
        for (Integer v : VALUE_SET) {
            set.add(v);
        }
    }

    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAddAndGrow() {
        HashSet<Integer> set = new HashSet<>();
        for (int v : VALUE_SET) {
            set.add(v);
        }
    }

    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureClone() {
        HashSet<Integer> set = (HashSet<Integer>) CONSTANT_SET.clone();
    }

    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureRemove() {
        HashSet<Integer> set = (HashSet<Integer>) CONSTANT_SET.clone();
        for (int v : VALUE_SET) {
            set.remove(v);
        }
    }

    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        HashSet<Integer> set = CONSTANT_SET;
        for (int v : VALUE_SET) {
            set.contains(v);
        }
    }

    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        HashSet<Integer> set = CONSTANT_SET;
        for (int v : NOT_IN_VALUE_SET) {
            set.contains(v);
        }
    }
}
