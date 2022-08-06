package ch.randelshofer.robinhood.jmh;

import ch.randelshofer.robinhood.RobinHoodHashSet;
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
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz, L3 Cache 12 MB, Memory 32 GB 2667 MHz DDR4
 * # -Xmx8g
 *
 * Benchmark          Mode  Cnt        Score        Error  Units
 * AddAll             avgt    2  2035811.479          ns/op
 * AddAllAndGrow      avgt    2  4860091.689          ns/op
 * Clone              avgt    2    54269.321          ns/op
 * CloneAndRemoveAll  avgt    2  1977180.581          ns/op
 * NewInstance        avgt    2        1.586          ns/op
 * RemoveAdd          avgt    2       53.482          ns/op
 * SuccessfulGet      avgt    2       10.249          ns/op
 * UnsuccessfulGet    avgt    2       16.128          ns/op
 *
 * The hashtable fits into the L3 cache.
 *
 * RobinHoodHashSet capacity:200000
 * RobinHoodHashSet fillRatio:0.5
 * RobinHoodHashSet loadFactor:0.5
 * RobinHoodHashSet costStats:IntSummaryStatistics{count=100000, sum=34303, min=0, average=0.343030, max=7}
 * </pre>
 */
@Fork(value = 1, jvmArgsAppend = {})
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class RobinHoodHashSetJmhBenchmark {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);


    private static final RobinHoodHashSet<BenchmarkDataSet.Key> CONSTANT_SET = new RobinHoodHashSet<>(DATA_SET.constantIdentitySet);

    static {
        System.out.println("RobinHoodHashSet size:" + CONSTANT_SET.size());
        System.out.println("RobinHoodHashSet capacity:" + CONSTANT_SET.getCapacity());
        System.out.println("RobinHoodHashSet fillRatio:" + CONSTANT_SET.getFillRatio());
        System.out.println("RobinHoodHashSet loadFactor:" + CONSTANT_SET.getLoadFactor());
        System.out.println("RobinHoodHashSet costStats:" + CONSTANT_SET.getCostStatistics());
    }

    @Benchmark
    public void mNewInstance() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = new RobinHoodHashSet<>(
                DATA_SET.constantIdentitySet.size() * 2,
                0.5f);
    }

    @Benchmark
    public void mAddAll() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = new RobinHoodHashSet<>(
                DATA_SET.constantIdentitySet.size() * 2,
                0.5f);
        boolean added = true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            added &= set.add(v);
        }
        if (!added || set.size() != DATA_SET.valuesInSet.length) {
            throw new AssertionError();
        }
    }

    @Benchmark
    public void mAddAllAndGrow() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = new RobinHoodHashSet<>(
                16,
                0.5f);
        boolean added = true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            added &= set.add(v);
        }
        if (!added || set.size() != DATA_SET.valuesInSet.length) {
            throw new AssertionError();
        }
    }

    @Benchmark
    public Object measureClone() {
        return CONSTANT_SET.clone();
    }

    @Benchmark
    public void mCloneAndRemoveAll() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET.clone();
        boolean removed = true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            removed &= set.remove(v);
        }
        if (!removed || set.size() != 0) {
            throw new AssertionError();
        }
    }

    @Benchmark
    public void mRemoveAdd() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.add(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void mSuccessfulGet() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void mUnsuccessfulGet() {
        RobinHoodHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesNotInSet[index]);
    }

}
