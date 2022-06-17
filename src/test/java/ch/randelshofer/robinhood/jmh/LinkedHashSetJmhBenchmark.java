package ch.randelshofer.robinhood.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

/**
 <pre>
 # JMH version: 1.28
 # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz

 Benchmark                                           Mode  Cnt        Score        Error  Units
 LinkedHashSetJmhBenchmark.measureAddAll             avgt   25  27_37885.389 ±  13970.390  ns/op
 LinkedHashSetJmhBenchmark.measureAddAllAndGrow      avgt   25  65_93559.077 ±  82907.057  ns/op
 LinkedHashSetJmhBenchmark.measureClone              avgt   25  27_00501.805 ±  31556.177  ns/op
 LinkedHashSetJmhBenchmark.measureCloneAndRemoveAll  avgt   25  61_02244.271 ± 140276.599  ns/op
 LinkedHashSetJmhBenchmark.measureRemoveAdd          avgt   25        61.085 ±      1.013  ns/op
 LinkedHashSetJmhBenchmark.measureSuccessfulGet      avgt   25        18.704 ±      0.354  ns/op
 LinkedHashSetJmhBenchmark.measureUnsuccessfulGet    avgt   25        10.310 ±      0.048  ns/op
 </pre>
 */
@Fork(value = 1, jvmArgsAppend = {})
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@SuppressWarnings({"unchecked", "UseBulkOperation"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class LinkedHashSetJmhBenchmark {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);

    private static final LinkedHashSet<BenchmarkDataSet.Key> CONSTANT_SET = new LinkedHashSet<>(DATA_SET.constantIdentitySet);

    static {
        System.out.println("LinkedHashSet size:" + CONSTANT_SET.size());
    }

    @Benchmark
    public void measureAddAll() {
        LinkedHashSet<BenchmarkDataSet.Key> set = new LinkedHashSet<>(
                DATA_SET.constantIdentitySet.size() * 2,
                0.75f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureAddAllAndGrow() {
        LinkedHashSet<BenchmarkDataSet.Key> set = new LinkedHashSet<>(
                0,
                0.75f);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureClone() {
        LinkedHashSet<BenchmarkDataSet.Key> set = (LinkedHashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
    }

    @Benchmark
    public void measureCloneAndRemoveAll() {
        LinkedHashSet<BenchmarkDataSet.Key> set = (LinkedHashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.remove(v);
        }
    }

    @Benchmark
    public void measureRemoveAdd() {
        LinkedHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.add(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public boolean measureSuccessfulGet() {
        LinkedHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        return set.contains(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public boolean measureUnsuccessfulGet() {
        LinkedHashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        return set.contains(DATA_SET.valuesNotInSet[index]);
    }
}
