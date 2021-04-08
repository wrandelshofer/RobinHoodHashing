package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * With capacity set to 134_000:
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 *
 * Benchmark        Mode  Cnt         Score        Error  Units
 * Add              avgt   25  35_35299.784 ±  98982.257  ns/op
 * AddAndGrow       avgt   25  72_30405.351 ± 139407.997  ns/op
 * Clone            avgt   25  38_88435.784 ± 184284.864  ns/op
 * Remove           avgt   25  93_96432.164 ±  62838.126  ns/op
 * SuccessfulGet    avgt   25  29_84498.237 ±  87343.811  ns/op
 * UnsuccessfulGet  avgt   25  35_11938.473 ± 100621.654  ns/op
 * </pre>
 */
public class LinkedRobinHoodHashSetJmhBenchmark {

    private static final Integer[] VALUE_SET = new Integer[100_000];
    private static final Integer[] NOT_IN_VALUE_SET = new Integer[100_000];

    private static final LinkedRobinHoodHashSet<Integer> CONSTANT_SET;

    public static final int CAPACITY = 134_000;

    static {
        Random rng = new Random(0);
        LinkedRobinHoodHashSet<Integer> set = new LinkedRobinHoodHashSet<>(CAPACITY);
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
    public void measureAddAndGrow() {
        LinkedRobinHoodHashSet<Integer> set = new LinkedRobinHoodHashSet<>();
        for (Integer v : VALUE_SET) {
            set.add(v);
        }
    }


    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAdd() {
        LinkedRobinHoodHashSet<Integer> set = new LinkedRobinHoodHashSet<>(CAPACITY);
        for (Integer v : VALUE_SET) {
            set.add(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        LinkedRobinHoodHashSet<Integer> set = CONSTANT_SET;
        for (Integer v : VALUE_SET) {
            set.contains(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        LinkedRobinHoodHashSet<Integer> set = CONSTANT_SET;
        for (Integer v : NOT_IN_VALUE_SET) {
            set.contains(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureRemove() {
        LinkedRobinHoodHashSet<Integer> set = CONSTANT_SET.clone();
        for (Integer v : VALUE_SET) {
            set.remove(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureClone() {
        LinkedRobinHoodHashSet<Integer> set = CONSTANT_SET.clone();
    }
}
