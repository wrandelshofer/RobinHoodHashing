package ch.randelshofer.robinhood;

import ch.randelshofer.robinhood.jmh.BenchmarkDataSet;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerifyJmhTest {
    private static class InstrumentedRobinHoodHashSet<E> extends RobinHoodHashSet<E> {
        private int tableAccess;

        public InstrumentedRobinHoodHashSet(Collection<? extends E> c, int initialCapacity, float loadFactor) {
            super(c, initialCapacity, loadFactor);
        }

        @Override
        protected E getKeyFromTable(int index) {
            tableAccess++;
            return super.getKeyFromTable(index);
        }
    }

    @Test
    public void verifyKeys() {
        BenchmarkDataSet dataSet = new BenchmarkDataSet(100_000, Integer.MIN_VALUE,
                Integer.MAX_VALUE, -1);

        VarianceStatistics intStats = new VarianceStatistics();
        VarianceStatistics hashStats = new VarianceStatistics();
        VarianceStatistics identityHashStats = new VarianceStatistics();
        for (BenchmarkDataSet.Key key : dataSet.valuesInSet) {
            intStats.accept(key.id());
            hashStats.accept(key.hashCode());
            identityHashStats.accept(System.identityHashCode(key));
        }
        System.out.println("idStats   :" + intStats);
        System.out.println("hashStats :" + hashStats);
        System.out.println("identStats:" + identityHashStats);
    }

    @Test
    public void verifyGet() {
        //BenchmarkDataSet dataSet = new BenchmarkDataSet(100_000, 0, 500_000);
        BenchmarkDataSet dataSet = new BenchmarkDataSet(100_000, Integer.MIN_VALUE,
                Integer.MAX_VALUE, -1);

        InstrumentedRobinHoodHashSet<BenchmarkDataSet.Key> set = new InstrumentedRobinHoodHashSet<>(dataSet.constantIdentitySet,
                dataSet.constantIdentitySet.size() * 3, 0.625f);

        System.out.println("RobinHoodHashSet size:" + set.size());
        System.out.println("RobinHoodHashSet capacity:" + set.getCapacity());
        System.out.println("RobinHoodHashSet fillRatio:" + set.getFillRatio());
        System.out.println("RobinHoodHashSet loadFactor:" + set.getLoadFactor());
        System.out.println("RobinHoodHashSet costStats:" + set.getCostStatistics());


        set.tableAccess = 0;
        VarianceStatistics vstats = new VarianceStatistics();
        int prevAccessCount = set.tableAccess;
        boolean contains = true;
        for (int index = 0; index < dataSet.valuesInSet.length; index++) {
            contains &= set.contains(dataSet.valuesInSet[index]);
            vstats.accept(set.tableAccess - prevAccessCount);
            prevAccessCount = set.tableAccess;
        }

        System.out.println("successful gets");
        System.out.println("  number of contains checks : " + dataSet.valuesInSet.length);
        System.out.println("  number of table accesses  : " + set.tableAccess);
        System.out.println("  number of table accesses %: " + (long) set.tableAccess * 100 / dataSet.valuesInSet.length);
        System.out.println("  stats: " + vstats);
        assertTrue(contains);

        set.tableAccess = 0;
        prevAccessCount = set.tableAccess;
        contains = false;
        vstats = new VarianceStatistics();
        for (int index = 0; index < dataSet.valuesNotInSet.length; index++) {
            contains |= set.contains(dataSet.valuesNotInSet[index]);
            vstats.accept(set.tableAccess - prevAccessCount);
            prevAccessCount = set.tableAccess;
        }

        System.out.println("unsuccessful gets");
        System.out.println("  number of contains checks : " + dataSet.valuesInSet.length);
        System.out.println("  number of table accesses  : " + set.tableAccess);
        System.out.println("  number of table accesses %: " + (long) set.tableAccess * 100 / dataSet.valuesInSet.length);
        System.out.println("  stats: " + vstats);
        assertFalse(contains);
    }
}
