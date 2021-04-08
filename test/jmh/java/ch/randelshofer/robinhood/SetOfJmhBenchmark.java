package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark        Mode  Cnt         Score       Error  Units
 * CopyOf           avgt   25  51_66687.178 ± 74694.286  ns/op
 * SuccessfulGet    avgt   25  10_50270.328 ± 43219.152  ns/op
 * UnsuccessfulGet  avgt   25  13_46460.043 ± 15932.896  ns/op
 * </pre>
 */
public class SetOfJmhBenchmark {

    private static final int[] VALUE_SET = new int[100_000];
    private static final int[] NOT_IN_VALUE_SET = new int[100_000];

    private static final HashSet<Integer> CONSTANT_SET;
    private static final Set<Integer> IMMUTABLE_SET;

    static {
        Random rng = new Random(0);
        HashSet<Integer> set = new HashSet<>(134_000);
        for (int i = 0; i < VALUE_SET.length; ) {
            int v = rng.nextInt(500_000);
            if (set.add(v)) {
                VALUE_SET[i++] = v;
            }
        }
        CONSTANT_SET = set;
        IMMUTABLE_SET = Set.copyOf(CONSTANT_SET);
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
    public void measureCopyOf() {
        Set.copyOf(CONSTANT_SET);
    }


    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        Set<Integer> set = IMMUTABLE_SET;
        for (int v : VALUE_SET) {
            set.contains(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        Set<Integer> set = IMMUTABLE_SET;
        for (int v : NOT_IN_VALUE_SET) {
            set.contains(v);
        }
    }
}
