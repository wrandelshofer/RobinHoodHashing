package ch.randelshofer.robinhood;

import java.util.Set;

class LinkedRobinHoodHashSetTest extends AbstractSetTest {
    @Override
    protected <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new LinkedRobinHoodHashSet<>(expectedMaxSize, maxLoadFactor);
    }

}