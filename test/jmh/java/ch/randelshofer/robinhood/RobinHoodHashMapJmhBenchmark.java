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
 * Benchmark          Mode  Cnt          Score        Error  Units
 * AddAll             avgt   25  24_30982.174 ±  47412.070  ns/op
 * AddAllAndGrow      avgt   25  77_73333.475 ± 133657.774  ns/op
 * Clone              avgt   25   1_12517.553 ±    215.790  ns/op
 * CloneAndRemoveAll  avgt   25  26_53936.501 ±  33377.562  ns/op
 * RemoveAdd          avgt   25        61.105 ±      0.798  ns/op
 * SuccessfulGet      avgt   25        10.519 ±      0.056  ns/op
 * UnsuccessfulGet    avgt   25        17.019 ±      0.234  ns/op
 *
 * The hashtable fits into the L3 cache.
 *
 * RobinHoodHashMap capacity:200000
 * RobinHoodHashMap fillRatio:0.5
 * RobinHoodHashMap loadFactor:0.5
 * RobinHoodHashMap costStats:IntSummaryStatistics{count=100000, sum=34303, min=0, average=0.343030, max=7}
 * </pre>
 */
//@Fork(value = 1, jvmArgsAppend = {})
//@Measurement(iterations = 2)
//@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class RobinHoodHashMapJmhBenchmark {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);


    private static final RobinHoodHashMap<BenchmarkDataSet.Key, Boolean> CONSTANT_SET = new RobinHoodHashMap<>(DATA_SET.constantIdentityMap, 0.5f);

    static {
        System.out.println("RobinHoodHashMap size:" + CONSTANT_SET.size());
        System.out.println("RobinHoodHashMap capacity:" + CONSTANT_SET.getCapacity());
        System.out.println("RobinHoodHashMap fillRatio:" + CONSTANT_SET.getFillRatio());
        System.out.println("RobinHoodHashMap loadFactor:" + CONSTANT_SET.getLoadFactor());
        System.out.println("RobinHoodHashMap costStats:" + CONSTANT_SET.getCostStatistics());
    }

    @Benchmark
    public void measureAddAll() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = new RobinHoodHashMap<>(
                DATA_SET.constantIdentitySet.size() * 2,
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
    public void measureAddAllAndGrow() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = new RobinHoodHashMap<>(
                0,
                0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.put(v,Boolean.TRUE);
        }
    }

    @Benchmark
    public Object measureClone() {
     return   (RobinHoodHashMap<BenchmarkDataSet.Key,Boolean>) CONSTANT_SET.clone();
    }

    @Benchmark
    public void measureCloneAndRemoveAll() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = (RobinHoodHashMap<BenchmarkDataSet.Key,Boolean>) CONSTANT_SET.clone();
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.remove(v);
        }
    }

    @Benchmark
    public void measureRemoveAdd() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.put(DATA_SET.valuesInSet[index],Boolean.TRUE);
    }

    @Benchmark
    public void measureSuccessfulGet() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.containsKey(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureUnsuccessfulGet() {
        RobinHoodHashMap<BenchmarkDataSet.Key,Boolean> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.containsKey(DATA_SET.valuesNotInSet[index]);
    }
}
