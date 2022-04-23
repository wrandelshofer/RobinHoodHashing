package ch.randelshofer.robinhood;

import java.util.Set;

class RobinHoodHashSetTest extends AbstractSetTest {


    @Override
    protected <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new RobinHoodHashSet<>(expectedMaxSize,maxLoadFactor);
    }

}