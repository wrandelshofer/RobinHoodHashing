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
 * Benchmark                                                      Mode  Cnt        Score       Error  Units
 * IdentityRobinHoodHashSetJmhBenchmark.measureAddAll             avgt   25  19_23601.464 ± 77940.241  ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureAddAllAndGrow      avgt   25  71_24312.791 ± 97971.171  ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureClone              avgt   25     86615.205 ±   683.388  ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureCloneAndRemoveAll  avgt   15  61_43186.776 ± 50804.304  ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureRemoveAdd          avgt   25        66.180 ±     1.816  ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureSuccessfulGet      avgt   25        12.066 ±     0.060  ns/op
 * IdentityRobinHoodHashSetJmhBenchmark.measureUnsuccessfulGet    avgt   25        18.870 ±     0.936  ns/op
 *
 * IdentityRobinHoodHashSet size:100000
 * IdentityRobinHoodHashSet capacity:300000
 * IdentityRobinHoodHashSet fillRatio:0.33333334
 * IdentityRobinHoodHashSet loadFactor:0.33333334
 * IdentityRobinHoodHashSet costStats:IntSummaryStatistics{count=100000, sum=24989, min=0, average=0.249890, max=6}
 * </pre>
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class IdentityRobinHoodHashSetJmhBenchmark  {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);


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
                DATA_SET.constantIdentitySet.size()*3,                0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureAddAllAndGrow() {
        IdentityRobinHoodHashSet<BenchmarkDataSet.Key> set = new IdentityRobinHoodHashSet<>(
                16,
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
