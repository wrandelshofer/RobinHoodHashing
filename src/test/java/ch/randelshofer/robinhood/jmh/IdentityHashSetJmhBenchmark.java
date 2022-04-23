package ch.randelshofer.robinhood.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * # VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark                                             Mode  Cnt   Score   Error  Units
 * IdentityHashSetJmhBenchmark.measureAddAll             avgt    2  3070578.078          ns/op
 * IdentityHashSetJmhBenchmark.measureAddAllAndGrow      avgt    2  6406697.484          ns/op
 * IdentityHashSetJmhBenchmark.measureClone              avgt    2   873753.743          ns/op
 * IdentityHashSetJmhBenchmark.measureCloneAndRemoveAll  avgt    2  3025901.831          ns/op
 * IdentityHashSetJmhBenchmark.measureRemoveAdd          avgt    2       54.085          ns/op
 * IdentityHashSetJmhBenchmark.measureSuccessfulGet      avgt    2        8.669          ns/op
 * IdentityHashSetJmhBenchmark.measureUnsuccessfulGet    avgt    2       13.436          ns/op
 * </pre>
 */
@Fork(value = 1, jvmArgsAppend = {})
@Measurement(iterations = 2)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class IdentityHashSetJmhBenchmark  {
    private static BenchmarkDataSet DATA_SET = new BenchmarkDataSet(100_000, 0, 500_000, -1);

    private static final Set<BenchmarkDataSet.Key> CONSTANT_SET = DATA_SET.constantIdentitySet;
    private static int index;

    @Benchmark
    public void measureAddAll() {
        IdentityHashMap<BenchmarkDataSet.Key, Boolean> identityMap = new IdentityHashMap<>(DATA_SET.size);
        Set<BenchmarkDataSet.Key> set = Collections.newSetFromMap(identityMap);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureAddAllAndGrow() {
        IdentityHashMap<BenchmarkDataSet.Key, Boolean> identityMap = new IdentityHashMap<>(16);
        Set<BenchmarkDataSet.Key> set = Collections.newSetFromMap(identityMap);
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            set.add(v);
        }
    }

    @Benchmark
    public void measureClone() {
        IdentityHashMap<BenchmarkDataSet.Key, Boolean> identityMap = (IdentityHashMap<BenchmarkDataSet.Key, Boolean>) DATA_SET.constantIdentityMap.clone();
    }

    @Benchmark
    public void measureCloneAndRemoveAll() {
        IdentityHashMap<BenchmarkDataSet.Key, Boolean> map = (IdentityHashMap<BenchmarkDataSet.Key, Boolean>) DATA_SET.constantIdentityMap.clone();
        for (BenchmarkDataSet.Key v : DATA_SET.valuesInSet) {
            map.remove(v);
        }
    }

    @Benchmark
    public void measureRemoveAdd() {
        Set<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.remove(DATA_SET.valuesInSet[index]);
        set.add(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureSuccessfulGet() {
        Set<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesInSet[index]);
    }

    @Benchmark
    public void measureUnsuccessfulGet() {
        Set<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index = DATA_SET.valuesNotInSet.length - index > 1 ? index + 1 : 0;
        set.contains(DATA_SET.valuesNotInSet[index]);
    }
}
