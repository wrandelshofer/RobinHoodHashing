package ch.randelshofer.robinhood.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 <pre>
 # JMH version: 1.28
 # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz

 HashSetJmhBenchmark.measureAddAll             avgt   25  18_77028.374 ±  58769.611  ns/op
 HashSetJmhBenchmark.measureAddAllAndGrow      avgt   25  56_69370.938 ±  94137.935  ns/op
 HashSetJmhBenchmark.measureClone              avgt   25  26_71116.575 ±  19545.953  ns/op
 HashSetJmhBenchmark.measureCloneAndRemoveAll  avgt   25  49_40933.351 ± 125261.650  ns/op
 HashSetJmhBenchmark.measureRemoveAdd          avgt   25        39.022 ±      1.357  ns/op
 HashSetJmhBenchmark.measureSuccessfulGet      avgt   25        12.392 ±      0.287  ns/op
 HashSetJmhBenchmark.measureUnsuccessfulGet    avgt   25         7.224 ±      0.025  ns/op
 HashSetJmhBenchmark.measureEqualsOfEqualSet   avgt    4  23_85546.743 ± 279149.625  ns/op
 </pre>
 */
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@Fork(value = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class HashSetJmhBenchmark {
    private static int index;
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);
    private static final HashSet<BenchmarkDataSet.Key> CONSTANT_SET = new HashSet<>(DATA_SET.constantIdentitySet);
    private static final HashSet<BenchmarkDataSet.Key> IDENTICAL_SET = new HashSet<>(DATA_SET.constantIdentitySet);

    static {
        System.out.println("HashSet size:" + CONSTANT_SET.size());
    }

    @Benchmark
    public void measureAddAllOneByOne() {
        HashSet<BenchmarkDataSet.Key> set = new HashSet<>(
                DATA_SET.constantIdentitySet.size() * 2,
                0.75f);
        boolean added = true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            added &= set.add(v);
        }
        if (!added || set.size() != DATA_SET.valuesInSet.length) {
            throw new AssertionError();
        }
    }

    @Benchmark
    public void measureAddAllOneByOneAndGrow() {
        HashSet<BenchmarkDataSet.Key> set = new HashSet<>(
                16,
                0.75f);
        boolean added = true;
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            added &= set.add(v);
        }
        if (!added || set.size() != DATA_SET.valuesInSet.length) {
            throw new AssertionError();
        }
    }

    @Benchmark
    public void measureClone() {
        HashSet<BenchmarkDataSet.Key> set = (HashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
    }

    @Benchmark
    public void measureCloneAndRemoveAllOneByOne() {
        HashSet<BenchmarkDataSet.Key> set = (HashSet<BenchmarkDataSet.Key>) CONSTANT_SET.clone();
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.remove(v);
        }
    }

    @Benchmark
    public void measureRemoveAdd() {
        HashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.add(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureSuccessfulGet() {
        HashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureUnsuccessfulGet() {
        HashSet<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesNotInSet[index]);
    }

    @Benchmark
    public boolean measureEqualsOfEqualSet() {
        return CONSTANT_SET.equals(IDENTICAL_SET);
    }

}
