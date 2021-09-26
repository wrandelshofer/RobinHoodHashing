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
 * # -Xmx16g
 *
 * Benchmark                                              Mode  Cnt         Score        Error  Units
 * RobinHoodHashMapJmhBenchmark.measureAddAll             avgt   25  16_02823.184 ±   9556.144  ns/op
 * RobinHoodHashMapJmhBenchmark.measureAddAllAndGrow      avgt   25  57_21952.598 ± 100088.927  ns/op
 * RobinHoodHashMapJmhBenchmark.measureClone              avgt   25   2_45129.602 ±    750.428  ns/op
 * RobinHoodHashMapJmhBenchmark.measureCloneAndRemoveAll  avgt   25  18_23900.086 ±  82570.691  ns/op
 * RobinHoodHashMapJmhBenchmark.measureRemoveAdd          avgt   25        43.480 ±      0.774  ns/op
 * RobinHoodHashMapJmhBenchmark.measureSuccessfulGet      avgt   25         6.998 ±      0.071  ns/op
 * RobinHoodHashMapJmhBenchmark.measureUnsuccessfulGet    avgt   25         8.765 ±      0.486  ns/op
 *
 * The hashtable fits into the L3 cache.
 *
 * RobinHoodHashMap capacity:400000
 * RobinHoodHashMap fillRatio:0.25
 * RobinHoodHashMap loadFactor:0.5
 * RobinHoodHashMap costStats:IntSummaryStatistics{count=100000, sum=8707, min=0, average=0.087070, max=3}
 * </pre>
 */
public class RobinHoodHashMapJmhBenchmark {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000);


    private static final RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> CONSTANT_SET = new RobinHoodHashMap<>(DATA_SET.constantIdentityMap);

    static {
        System.out.println("RobinHoodHashMap size:" + CONSTANT_SET.size());
        System.out.println("RobinHoodHashMap capacity:" + CONSTANT_SET.getCapacity());
        System.out.println("RobinHoodHashMap fillRatio:" + CONSTANT_SET.getFillRatio());
        System.out.println("RobinHoodHashMap loadFactor:" + CONSTANT_SET.getLoadFactor());
        System.out.println("RobinHoodHashMap costStats:" + CONSTANT_SET.getCostStatistics());
    }
/*
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureNewInstance() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = new RobinHoodHashMap<>(
                DATA_SET.constantIdentitySet.size()*4,
                0.5f);
    }
    */
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAddAll() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = new RobinHoodHashMap<>(
                DATA_SET.constantIdentitySet.size()*4,
                0.5f);
        boolean added=true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            added&=set.put(v,Boolean.TRUE)==null;
        }
        if (!added||set.size()!=DATA_SET.valuesInSet.length) {
            throw new AssertionError();
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureAddAllAndGrow() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = new RobinHoodHashMap<>(
                0,
                0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.put(v,Boolean.TRUE);
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public Object measureClone() {
     return   (RobinHoodHashMap<BenchmarkDataSet.Key,Boolean>) CONSTANT_SET.clone();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureCloneAndRemoveAll() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = (RobinHoodHashMap<BenchmarkDataSet.Key,Boolean>) CONSTANT_SET.clone();
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.remove(v);
        }
    }
/*
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureRemoveAdd() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.put(DATA_SET.valuesInSet[index],Boolean.TRUE);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.containsKey(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.containsKey(DATA_SET.valuesNotInSet[index]);
    }
*/

}
