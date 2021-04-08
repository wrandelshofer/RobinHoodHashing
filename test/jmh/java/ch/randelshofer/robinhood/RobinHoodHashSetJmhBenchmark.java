package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * With capacity set to 262_144:
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark        Mode  Cnt         Score       Error  Units
 * Clone            avgt   25    _73802.372 ±   932.618  ns/op
 * Remove           avgt   25  54_50882.281 ± 92446.490  ns/op
 * SuccessfulGet    avgt   25  13_59960.953 ± 75048.110  ns/op
 * UnsuccessfulGet  avgt   25  16_49366.959 ± 78153.302  ns/op
 * </pre>
 * With capacity set to 134_000:
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 16, OpenJDK 64-Bit Server VM, 16+36 (Azul Zulu)
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark        Mode  Cnt         Score       Error  Units
 * Add              avgt   25  27_71320.175 ± 32622.963  ns/op
 * AddAndGrow       avgt   25  64_36769.576 ± 77519.754  ns/op
 * Clone            avgt   25    _37855.608 ±   206.238  ns/op
 * Remove           avgt   25  57_99296.564 ± 41112.763  ns/op
 * SuccessfulGet    avgt   25  34_33375.830 ± 45335.358  ns/op
 * UnsuccessfulGet  avgt   25  38_44692.219 ± 22035.735  ns/op
 * </pre>
 */
public class RobinHoodHashSetJmhBenchmark {

    private static final Integer[] VALUE_SET = new Integer[100_000];
    private static final Integer[] NOT_IN_VALUE_SET = new Integer[100_000];

    private static final RobinHoodHashSet<Integer> CONSTANT_SET;

    public static final int CAPACITY = 134_000;

    static {
        Random rng = new Random(0);
        RobinHoodHashSet<Integer> set = new RobinHoodHashSet<>(CAPACITY);
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
        System.out.println("CONSTANT_SET.size     : "+set.size());
        System.out.println("CONSTANT_SET.capacity : "+set.getCapacity());
        System.out.println("CONSTANT_SET.costStats; "+set.getCostStatistics());
    }

   @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAdd() {
        RobinHoodHashSet<Integer> set = new RobinHoodHashSet<>(CAPACITY);
        for (Integer v : VALUE_SET) {
            set.add(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAddAndGrow() {
        RobinHoodHashSet<Integer> set = new RobinHoodHashSet<>();
        for (Integer v : VALUE_SET) {
            set.add(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureClone() {
        RobinHoodHashSet<Integer> set = CONSTANT_SET.clone();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureRemove() {
        RobinHoodHashSet<Integer> set = CONSTANT_SET.clone();
        for (Integer v : VALUE_SET) {
            set.remove(v);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        RobinHoodHashSet<Integer> set = CONSTANT_SET;
        for (Integer v : VALUE_SET) {
            set.contains(v);
        }
    }

   @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        RobinHoodHashSet<Integer> set = CONSTANT_SET;
        for (Integer v : NOT_IN_VALUE_SET) {
            set.contains(v);
        }
    }
}
