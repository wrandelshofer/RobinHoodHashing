package ch.randelshofer.robinhood;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * # JMH version: 1.28
 * #VM version: JDK 17, OpenJDK 64-Bit Server VM, 17+35-2724
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 *
 * Benchmark        Mode  Cnt        Score       Error  Units
 * Add              avgt   25
 * SetOfJmhBenchmark.measureSuccessfulGet    avgt   25   9.504 ± 0.149  ns/op
 * SetOfJmhBenchmark.measureUnsuccessfulGet  avgt   25  15.967 ± 0.209  ns/op
 * </pre>
 */
public class SetOfJmhBenchmark  {
private static int index;
    private static BenchmarkDataSet DATA_SET =new BenchmarkDataSet(100_000, 0, 500_000);
    private static final Set<BenchmarkDataSet.Key> CONSTANT_SET = Set.copyOf(DATA_SET.constantIdentitySet);




    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureClone() {
        Set.copyOf(DATA_SET.constantIdentitySet);
    }

   @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureSuccessfulGet() {
        Set<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index= DATA_SET.valuesInSet.length-index>1?index+1:0;
        set.contains(DATA_SET.valuesInSet[index]);
    }
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void measureUnsuccessfulGet() {
        Set<BenchmarkDataSet.Key> set = CONSTANT_SET;
        index= DATA_SET.valuesNotInSet.length-index>1?index+1:0;
        set.contains(DATA_SET.valuesNotInSet[index]);
    }

}
