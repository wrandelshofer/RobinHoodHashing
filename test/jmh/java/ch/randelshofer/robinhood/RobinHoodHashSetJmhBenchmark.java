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
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz, L3 Cache 12 MB, Memory 32 GB 2667 MHz DDR4
 * # -Xmx8g
 *
 * Benchmark                                             Mode  Cnt        Score        Error  Units
 * RobinHoodHashSetJmhBenchmark.measureAddAll            avgt   25  14_47494.150 ± 128516.986  ns/op
 * RobinHoodHashSetJmhBenchmark.measureAddAllAndGrow     avgt   25  61_89208.353 ±  68023.599  ns/op
 * RobinHoodHashSetJmhBenchmark.measureClone             avgt   25     85105.879 ±    145.628  ns/op
 * RobinHoodHashSetJmhBenchmark.measureCloneAndRemoveAll avgt   25  15_54209.786 ± 30035.416  ns/op
 * RobinHoodHashSetJmhBenchmark.measureRemoveAdd         avgt   25        35.566 ±     1.039  ns/op
 * RobinHoodHashSetJmhBenchmark.measureSuccessfulGet     avgt   25         9.225 ±      0.791  ns/op
 * RobinHoodHashSetJmhBenchmark.measureUnsuccessfulGet   avgt   25        14.319 ±      1.003  ns/op
 *
 * The hashtable fits into the L3 cache.
 *
 * RobinHoodHashSet capacity:400000
 * RobinHoodHashSet fillRatio:0.25
 * RobinHoodHashSet loadFactor:0.5
 * RobinHoodHashSet costStats:IntSummaryStatistics{count=100000, sum=8707, min=0, average=0.087070, max=3}
 * </pre>
 */
public class RobinHoodHashSetJmhBenchmark {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000);


    private static final RobinHoodHashSet<BenchmarkDataSet.Key> CONSTANT_SET = new RobinHoodHashSet<>(DATA_SET.constantIdentitySet);

    static {
        System.out.println("RobinHoodHashSet size:" + CONSTANT_SET.size());
        System.out.println("RobinHoodHashSet capacity:" + CONSTANT_SET.getCapacity());
        System.out.println("RobinHoodHashSet fillRatio:" + CONSTANT_SET.getFillRatio());
        System.out.println("RobinHoodHashSet loadFactor:" + CONSTANT_SET.getLoadFactor());
        System.out.println("RobinHoodHashSet costStats:" + CONSTANT_SET.getCostStatistics());
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureNewInstance() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = new RobinHoodHashSet<>(
                DATA_SET.constantIdentitySet.size()*4,
                0.5f);
    }
    /*
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAddAll() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = new RobinHoodHashSet<>(
                DATA_SET.constantIdentitySet.size()*4,
                0.5f);
        boolean added=true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            added&=set.add(v);
        }
        if (!added||set.size()!=DATA_SET.valuesInSet.length) {
            throw new AssertionError();
        }
    }
/*
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAddAllAndGrow() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = new RobinHoodHashSet<>(
                0,
                0.5f);
        boolean added=true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            added&=set.add(v);
        }
        if (!added||set.size()!=DATA_SET.valuesInSet.length) {
            throw new AssertionError();
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public Object measureClone() {
        return CONSTANT_SET.clone();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureCloneAndRemoveAll() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET.clone();
        boolean removed=true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            removed&=set.remove(v);
        }
        if (!removed||set.size()!=0) {
            throw new AssertionError();
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureRemoveAdd() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.add(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesNotInSet[index]);
    }
*/

}
