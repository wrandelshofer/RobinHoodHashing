package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                                      Mode  Cnt        Score       Error  Units
 * IdentityRobinHoodHashSetJmhBenchmark.measureAddAll             avgt    2  1678617.788          ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureAddAllAndGrow      avgt    2  5043434.842          ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureClone              avgt    2    71799.364          ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureCloneAndRemoveAll  avgt    2  2148396.884          ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureRemoveAdd          avgt    2       46.323          ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureSuccessfulGet      avgt    2        9.600          ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureUnsuccessfulGet    avgt    2       14.943          ns/op
 *
 * IdentityRobinHoodHashSet capacity:262144
 * IdentityRobinHoodHashSet fillRatio:0.38146973
 * IdentityRobinHoodHashSet loadFactor:0.5
 * IdentityRobinHoodHashSet costStats:IntSummaryStatistics{count=100000, sum=30402, min=0, average=0.304020, max=6}
 * </pre>
 */
@Fork(value = 1, jvmArgsAppend = {})
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class IdentityRobinHoodHashSetJmhBenchmark  {
    private static int index;
    private final static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);


    private static final IdentityRobinHoodHashSet<BenchmarkDataSet.Key> CONSTANT_SET = new IdentityRobinHoodHashSet<>(DATA_SET.constantIdentitySet);
    static {
        System.out.println("IdentityRobinHoodHashSet size:" + CONSTANT_SET.size());
        System.out.println("IdentityRobinHoodHashSet capacity:" + CONSTANT_SET.getCapacity());
        System.out.println("IdentityRobinHoodHashSet fillRatio:" + CONSTANT_SET.getFillRatio());
        System.out.println("IdentityRobinHoodHashSet loadFactor:" + CONSTANT_SET.getLoadFactor());
        System.out.println("IdentityRobinHoodHashSet costStats:" + CONSTANT_SET.getCostStatistics());
    }

    @Benchmark
    public void measureAddAll() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = new IdentityRobinHoodHashSet<>(
                DATA_SET.constantIdentitySet.size() * 2, 0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureAddAllAndGrow() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = new IdentityRobinHoodHashSet<>(
                0,
                0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureClone() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = (IdentityRobinHoodHashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
    }

    @Benchmark
    public void measureCloneAndRemoveAll() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = (IdentityRobinHoodHashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.remove(v);
        }
    }


    @Benchmark
    public void measureRemoveAdd() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.add(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureSuccessfulGet() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureUnsuccessfulGet() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesNotInSet[index]);
    }

}
