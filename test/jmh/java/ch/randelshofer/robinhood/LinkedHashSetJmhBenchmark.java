package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark        Mode  Cnt         Score        Error  Units
 * Add              avgt   25  26_66328.137 ±  16793.643  ns/op
 * AddAndGrow       avgt   25  59_48198.331 ± 174289.648  ns/op
 * Clone            avgt   25  22_56326.641 ±  25625.761  ns/op
 * Remove           avgt   25  38_43179.229 ± 120510.939  ns/op
 * SuccessfulGet    avgt   25   7_59121.216 ±  33160.601  ns/op
 * UnsuccessfulGet  avgt   25   6_24672.767 ±   1875.119  ns/op
 * </pre>
 */
public class LinkedHashSetJmhBenchmark {

    private static final int[] VALUE_SET = new int[100_000];
    private static final int[] NOT_IN_VALUE_SET = new int[100_000];

    private static final LinkedHashSet<Integer> CONSTANT_SET;

    static {
        Random rng = new Random(0);
        LinkedHashSet<Integer> set = new LinkedHashSet<>(134_000);
        for (int i = 0; i < VALUE_SET.length; ) {
            int v = rng.nextInt(500_000);
            if (set.add(v)) {
                VALUE_SET[i++] = v;
            }
        }
        CONSTANT_SET = set;
        for (int i = 0; i < VALUE_SET.length; ) {
            int v = rng.nextInt(500_000);
            if (!set.contains(v)) {
                NOT_IN_VALUE_SET[i++] = v;
            }
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAdd() {
        RobinHoodHashSet<Integer> set = new RobinHoodHashSet<>(134_000);
        for (Integer v : VALUE_SET) {
            set.add(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAddAndGrow() {
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        for (int v : VALUE_SET) {
            set.add(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureClone() {
        LinkedHashSet<Integer> set = (LinkedHashSet<Integer>) CONSTANT_SET.clone();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureRemove() {
        LinkedHashSet<Integer> set = (LinkedHashSet<Integer>) CONSTANT_SET.clone();
        for (int v : VALUE_SET) {
            set.remove(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        LinkedHashSet<Integer> set = CONSTANT_SET;
        for (int v : VALUE_SET) {
            set.contains(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        LinkedHashSet<Integer> set = CONSTANT_SET;
        for (int v : NOT_IN_VALUE_SET) {
            set.contains(v);
        }
    }
}
