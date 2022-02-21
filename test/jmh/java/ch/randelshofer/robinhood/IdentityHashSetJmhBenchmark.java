package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

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
 * Benchmark                                            Mode  Cnt   Score   Error  Units
 * IdentityHashSetJmhBenchmark.measureAddAll            avgt   25  41_47140.376 ± 87510.859  ns/op
 * IdentityHashSetJmhBenchmark.measureAddAllAndGrow     avgt   25  63_04410.974 ± 72965.884  ns/op
 * IdentityHashSetJmhBenchmark.measureClone             avgt   25  10_09419.270 ± 56729.792  ns/op
 * IdentityHashSetJmhBenchmark.measureCloneAndRemoveAll avgt   25  35_24395.592 ± 365843.009  ns/op
 * IdentityHashSetJmhBenchmark.measureRemoveAdd         avgt   25        62.109 ±     3.997  ns/op
 * IdentityHashSetJmhBenchmark.measureSuccessfulGet     avgt   25        11.029 ±     0.173  ns/op
 * IdentityHashSetJmhBenchmark.measureUnsuccessfulGet   avgt   25        15.119 ±     0.360  ns/op
 * </pre>
 */
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
