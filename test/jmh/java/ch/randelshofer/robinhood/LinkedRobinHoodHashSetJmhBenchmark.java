package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                                    Mode  Cnt        Score        Error  Units
 * LinkedRobinHoodHashSetJmhBenchmark.measureAddAll             avgt   25  24_34411.214 ±  28144.986  ns/op
 * LinkedRobinHoodHashSetJmhBenchmark.measureAddAllAndGrow      avgt   25  48_91589.453 ± 303555.972  ns/op
 * LinkedRobinHoodHashSetJmhBenchmark.measureClone              avgt   25  17_54426.236 ±  38308.059  ns/op
 * LinkedRobinHoodHashSetJmhBenchmark.measureCloneAndRemoveAll  avgt   25  46_64801.262 ±  90092.910  ns/op
 * LinkedRobinHoodHashSetJmhBenchmark.measureRemoveAdd          avgt   25      108.496 ±      5.206  ns/op
 * LinkedRobinHoodHashSetJmhBenchmark.measureSuccessfulGet      avgt   25       10.054 ±      0.070  ns/op
 * LinkedRobinHoodHashSetJmhBenchmark.measureUnsuccessfulGet    avgt   25        9.239 ±      0.481  ns/op
 *
 * LinkedRobinHoodHashSet capacity:400000
 * LinkedRobinHoodHashSet fillRatio:0.25
 * LinkedRobinHoodHashSet loadFactor:0.5
 * LinkedRobinHoodHashSet costStats:IntSummaryStatistics{count=100000, sum=8707, min=0, average=0.087070, max=3}
 * </pre>
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class LinkedRobinHoodHashSetJmhBenchmark {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);


    private static final LinkedRobinHoodHashSet<BenchmarkDataSet.Key> CONSTANT_SET = new LinkedRobinHoodHashSet<>(DATA_SET.constantIdentitySet);
    static {
        System.out.println("LinkedRobinHoodHashSet size:" + CONSTANT_SET.size());
        System.out.println("LinkedRobinHoodHashSet capacity:" + CONSTANT_SET.getCapacity());
        System.out.println("LinkedRobinHoodHashSet fillRatio:" + CONSTANT_SET.getFillRatio());
        System.out.println("LinkedRobinHoodHashSet loadFactor:" + CONSTANT_SET.getLoadFactor());
        System.out.println("LinkedRobinHoodHashSet costStats:" + CONSTANT_SET.getCostStatistics());
    }

    @Benchmark
    public void measureAddAll() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = new LinkedRobinHoodHashSet<>(
                DATA_SET.constantIdentitySet.size()*2,
                0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureAddAllAndGrow() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = new LinkedRobinHoodHashSet<>(
                16,
                0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureClone() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = (LinkedRobinHoodHashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
    }

    @Benchmark
    public void measureCloneAndRemoveAll() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = (LinkedRobinHoodHashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.remove(v);
        }
    }

    @Benchmark
    public void measureRemoveAdd() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.add(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureSuccessfulGet() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureUnsuccessfulGet() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesNotInSet[index]);
    }

}
