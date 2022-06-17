package ch.randelshofer.robinhood.jmh;

import ch.randelshofer.robinhood.LinkedRobinHoodHashSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 <pre>
 # JMH version: 1.28
 # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz

 Benchmark                                                    Mode  Cnt        Score        Error  Units
 LinkedRobinHoodHashSetJmhBenchmark.measureAddAll             avgt    2  2648074.354          ns/op
 LinkedRobinHoodHashSetJmhBenchmark.measureAddAllAndGrow      avgt    2  4538952.170          ns/op
 LinkedRobinHoodHashSetJmhBenchmark.measureClone              avgt    2  2348282.888          ns/op
 LinkedRobinHoodHashSetJmhBenchmark.measureCloneAndRemoveAll  avgt    2  6156654.113          ns/op
 LinkedRobinHoodHashSetJmhBenchmark.measureRemoveAdd          avgt    2      142.222          ns/op
 LinkedRobinHoodHashSetJmhBenchmark.measureSuccessfulGet      avgt    2       15.949          ns/op
 LinkedRobinHoodHashSetJmhBenchmark.measureUnsuccessfulGet    avgt    2       24.237          ns/op

 LinkedRobinHoodHashSet capacity:200000
 LinkedRobinHoodHashSet fillRatio:0.5
 LinkedRobinHoodHashSet loadFactor:0.5
 LinkedRobinHoodHashSet costStats:IntSummaryStatistics{count=100000, sum=34303, min=0, average=0.343030, max=7}
 </pre>
 */
@Fork(value = 1, jvmArgsAppend = {})
@Measurement(iterations = 2)
@Warmup(iterations = 2)
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
                DATA_SET.constantIdentitySet.size() * 2,
                0.5f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureAddAllAndGrow() {
        LinkedRobinHoodHashSet<BenchmarkDataSet.Key> set = new LinkedRobinHoodHashSet<>(
                0,
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
