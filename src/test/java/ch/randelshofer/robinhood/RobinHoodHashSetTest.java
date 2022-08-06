package ch.randelshofer.robinhood;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RobinHoodHashSetTest extends AbstractSetTest {


    @Override
    protected <T> RobinHoodHashSet<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new RobinHoodHashSet<>(expectedMaxSize, maxLoadFactor);
    }

    @Test
    public void shouldBeAbleToResize() {
        RobinHoodHashSet<Integer> set = create(5, 0.25f);
        IntStream.range(0, 5).forEach(set::add);
        assertEquals(5 / 0.25f, set.getCapacity());
        set.sizeToFit(0.5f);
        assertEquals(5 / 0.5f, set.getCapacity());
    }

    @Test
    public void shouldBeAbleToResizeWithoutNumericalOverflow() {
        RobinHoodHashSet<Integer> set = create(100, 0.1f);
        IntStream.range(10, 20).forEach(set::add);
        assertEquals(10, set.size());
        assertEquals(1000, set.getCapacity());

        set.sizeToFit(0.1f);
        assertEquals(10, set.size());
        assertEquals(100, set.getCapacity());
        assertTrue(set.contains(10));
        assertTrue(set.contains(19));

        set.sizeToFit(0.01f);
        assertEquals(10, set.size());
        assertEquals(1000, set.getCapacity());
        assertTrue(set.contains(10));
        assertTrue(set.contains(19));

        // WHEN Add value right at the end of the array
        //       and resize is performed
        set.add(1000 - 1);
        set.sizeToFit(0.1f);

        // THEN
        assertEquals(11, set.size());
        assertEquals(110, set.getCapacity());
        assertTrue(set.contains(10));
        assertTrue(set.contains(19));
        assertTrue(set.contains(1000 - 1));
    }
}