package ch.randelshofer.robinhood;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSetTest {

protected abstract <T> Set<T> create(int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) ;
    @Test
    public void testAdd1Element() {
        Set<Integer> set = create(16,0.75f,2.0f,0.125f);

        set.add(5);

        assertTrue(set.contains(5));
        assertFalse(set.contains(0));
    }

    @Test
    public void testAddAndRemoveElementsWithCollisions() {
        Set<Integer> set = create(32,1f,1f,0f);

        List<Integer> list = IntStream.range(0, 26).boxed().collect(Collectors.toList());


        for (int i = 0; i < list.size(); i++) {
            set.add(list.get(i));
            assertTrue(set.containsAll(list.subList(0, i + 1)));
            assertFalse(containsAny(set, list.subList(i + 1, list.size())));
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.println("removing " + list.get(i));
            boolean remove = set.remove(list.get(i));
            assertTrue(remove, "removing " + list.get(i));

            assertTrue(set.containsAll(list.subList(i + 1, list.size())), "failed to remove " + list.get(i));
            assertFalse(containsAny(set, list.subList(0, i)));
        }
    }

    @Test
    public void testAddAndRemoveElementsWithCollisionsAndGrowAndShrink() {
        Set<Integer> set = create(5,0.75f,1.5f,0.125f);

        List<Integer> list = IntStream.range(0, 26).boxed().collect(Collectors.toList());


        for (int i = 0; i < list.size(); i++) {
            System.out.println("adding " + list.get(i));
            set.add(list.get(i));
            assertTrue(set.containsAll(list.subList(0, i + 1)));
            assertFalse(containsAny(set, list.subList(i + 1, list.size())));
        }

       for (int i = 0; i < list.size(); i++) {
            System.out.println("removing " + list.get(i));
            boolean remove = set.remove(list.get(i));
            assertTrue(remove, "removing " + list.get(i));

            assertTrue(set.containsAll(list.subList(i + 1, list.size())), "failed to remove " + list.get(i));
            assertFalse(containsAny(set, list.subList(0, i)));
        }
    }

    @Test
    public void testAddAndRemoveElementsWithoutCollisions() {
       Set<Integer> set = create(16,1f,1f,0f);

        List<Integer> list = Arrays.asList(5, 6, 7, 8);

        for (int i = 0; i < list.size(); i++) {
            set.add(list.get(i));
            assertTrue(set.containsAll(list.subList(0, i + 1)));
            assertFalse(containsAny(set, list.subList(i + 1, list.size())));
        }

        for (int i = 0; i < list.size(); i++) {
            boolean remove = set.remove(list.get(i));
            assertTrue(remove, "removing " + list.get(i));
            assertTrue(set.containsAll(list.subList(i + 1, list.size())), "failed to remove " + list.get(i));
            assertFalse(containsAny(set, list.subList(0, i)));
        }
    }

    private <T> boolean containsAny(Set<T> set, Collection<T> c) {
        for (T e : c) {
            if (set.contains(e)) {
                return true;
            }
        }
        return false;
    }

    ///-------------

    public void doTestBenchmark(Supplier<Set<Integer>> factory) {
        List<Integer> integers = new Random(0).ints(100_000,0,200_000)
                .boxed().collect(Collectors.toList());
        //warmup
        for (int j = 0; j < 1024; j++) {
            Set<Integer> set = factory.get();
            for (Integer i:integers) {
                set.add(i);
            }
            if (j==0){System.out.println("set size="+set.size());
                if (set instanceof RobinHoodHashSetTwoArrays) {
                    ((RobinHoodHashSetTwoArrays)set).dumpStats();
                }
            }
        }
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int j = 0; j < 32; j++) {
            Set<Integer> set = factory.get();
            long start = System.nanoTime();
            for (Integer i :integers) {
                set.add(i);
            }
            long end = System.nanoTime();
            stats.accept((end - start) / 1_000d);
        }
        System.out.println(factory.get().getClass());
        System.out.println(stats);
    }
    public void doTestBenchmarkShrink(Supplier<Set<Integer>> factory) throws Exception {
        Random rng = new Random(0);
        List<Integer> initialIntegers = rng.ints(100_000,0,100_000)
                .boxed().collect(Collectors.toList());
        List<Integer> removeIntegers = rng.ints(100_000,0,100_000)
                .boxed().collect(Collectors.toList());
        Set<Integer> initialSet = factory.get();
        initialSet.addAll(initialIntegers);
        //warmup
        for (int j = 0; j < 1024; j++) {

            Set<Integer> set= (Set<Integer>) initialSet.getClass().getDeclaredMethod("clone").invoke(initialSet);;
        set.removeAll(removeIntegers);
            if (j==0){System.out.println("set size="+set.size());
                HashSet<Integer> expected=new HashSet<>();
                expected.addAll(initialIntegers);
                expected.removeAll(removeIntegers);
                assertEquals(expected,set);
            }
        }
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int j = 0; j < 32; j++) {
            Set<Integer> set= (Set<Integer>) initialSet.getClass().getDeclaredMethod("clone").invoke(initialSet);;
            long start = System.nanoTime();
            set.removeAll(removeIntegers);
            long end = System.nanoTime();
            stats.accept((end - start) / 1_000d);
        }
        System.out.println(factory.get().getClass());
        System.out.println(stats);
    }
  




}
