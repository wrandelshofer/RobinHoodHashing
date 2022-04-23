package ch.randelshofer.robinhood;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSetTest {

    public static final Key FIVE = new Key(5);
    public static final Key THREE = new Key(3);
    public static final Key ZERO = new Key(0);
    public static final Key SIX = new Key(6);
    public static final Key SEVEN = new Key(7);
    public static final Key EIGHT = new Key(8);

    protected abstract <T> Set<T> create(int expectedMaxSize, float maxLoadFactor);

    @Test
    public void testAdd1ElementWithZeroInitialCapacity() {
        Set<Key> set = create(0, 0.75f);

        set.add(FIVE);

        assertTrue(set.contains(FIVE));
        assertFalse(set.contains(THREE));
    }

    @Test
    public void testAdd1ElementWith1InitialCapacityAnd100PercentLoadFactor() {
        Set<Key> set = create(1, 1f);

        set.add(FIVE);

        assertTrue(set.contains(FIVE));
        assertFalse(set.contains(THREE));
    }

    @Test
    public void testAdd1Element() {
        Set<Key> set = create(16, 0.75f);

        set.add(FIVE);

        assertTrue(set.contains(FIVE));
        assertFalse(set.contains(ZERO));
    }

    @Test
    public void testAddAndRemoveElementsOnGrowableTableOfInitialCapacity0() {
        doTestAddAndRemoveElementsOnGrowableTable(create(0, 0.625f));
    }

    @Test
    public void testAddAndRemoveElementsOnGrowableTableOfInitialCapacity20() {
        doTestAddAndRemoveElementsOnGrowableTable(create(20, 0.625f));
    }

    @Test
    public void testAddAndRemoveElementsOnGrowableTableOfInitialCapacity0And100PercentLoadFactor() {
        doTestAddAndRemoveElementsOnGrowableTable(create(0, 1f));
    }


    public void doTestAddAndRemoveElementsOnGrowableTable(Set<Key> set) {
        set.add(FIVE);
        set.add(SIX);
        set.add(SEVEN);

        assertTrue(set.contains(FIVE));
        assertTrue(set.contains(SIX));
        assertTrue(set.contains(SEVEN));
        assertFalse(set.contains(ZERO));

        set.remove(FIVE);
        set.remove(SIX);
        set.remove(SEVEN);

        assertFalse(set.contains(FIVE));
        assertFalse(set.contains(SIX));
        assertFalse(set.contains(SEVEN));
        assertFalse(set.contains(ZERO));
    }


    @Test
    public void testRemoveElementsWithIterator() {
        Set<Key> set = create(21, 1f);

        set.add(FIVE);
        set.add(SIX);
        set.add(SEVEN);
        set.add(EIGHT);

        assertTrue(set.contains(FIVE));
        assertTrue(set.contains(SIX));
        assertTrue(set.contains(SEVEN));
        assertTrue(set.contains(EIGHT));

        for (Iterator<Key> it = set.iterator(); it.hasNext(); ) {
            Key k = it.next();
            if (k == SIX) {
                it.remove();
            }
        }
        assertTrue(set.contains(FIVE));
        assertFalse(set.contains(SIX));
        assertTrue(set.contains(SEVEN));
        assertTrue(set.contains(EIGHT));

        for (Iterator<Key> it = set.iterator(); it.hasNext(); ) {
            Key k = it.next();
            if (k == EIGHT) {
                it.remove();
            }
        }
        assertTrue(set.contains(FIVE));
        assertFalse(set.contains(SIX));
        assertTrue(set.contains(SEVEN));
        assertFalse(set.contains(EIGHT));

        for (Iterator<Key> it = set.iterator(); it.hasNext(); ) {
            Key k = it.next();
            it.remove();
        }
        assertFalse(set.contains(FIVE));
        assertFalse(set.contains(SIX));
        assertFalse(set.contains(SEVEN));
        assertFalse(set.contains(EIGHT));
    }

    @Test
    public void testAddAndRemoveElementsWithCollisions() {
        Set<Key> set = create(32, 1f);

        List<Key> list = IntStream.range(0, 26).mapToObj(Key::new).collect(Collectors.toList());


        for (int i = 0; i < list.size(); i++) {
            System.out.println("adding " + list.get(i));
            set.add(list.get(i));
            if (!set.containsAll(list.subList(0, i + 1))) {
                System.out.println(set);
            }
            assertTrue(set.containsAll(list.subList(0, i + 1)));
            assertFalse(containsAny(set, list.subList(i + 1, list.size())));
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.println("removing " + list.get(i));
            boolean remove = set.remove(list.get(i));
            assertTrue(remove, "successfully removed " + list.get(i));

            //   assertTrue(set.contains(list.get(22)));

            assertTrue(set.containsAll(list.subList(i + 1, list.size())), "failed to remove " + list.get(i)
                    + "\nlist:" + list.subList(i + 1, list.size())
                    + "\nset :" + set
            );
            assertFalse(containsAny(set, list.subList(0, i)));
        }
    }

    @Test
    public void testAddAndRemoveElementsWithCollisionsAndGrowAndShrink() {
        Set<Key> set = create(5, 0.75f);

        List<Key> list = IntStream.range(0, 26).mapToObj(Key::new).collect(Collectors.toList());


        for (int i = 0; i < list.size(); i++) {
            //System.out.println("adding " + list.get(i));
            set.add(list.get(i));
            assertTrue(set.containsAll(list.subList(0, i + 1)));
            assertFalse(containsAny(set, list.subList(i + 1, list.size())));
        }

        for (int i = 0; i < list.size(); i++) {
            //System.out.println("removing " + list.get(i));
            boolean remove = set.remove(list.get(i));
            assertTrue(remove, "removing " + list.get(i));

            assertTrue(set.containsAll(list.subList(i + 1, list.size())), "failed to remove " + list.get(i));
            assertFalse(containsAny(set, list.subList(0, i)));
        }
    }

    @Test
    public void testAddAndRemoveElementsWithoutCollisions() {
        Set<Key> set = create(16, 1f);

        List<Key> list = Arrays.asList(FIVE, SIX, SEVEN, EIGHT);

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

    public static record Key(int id) {
    }
}
