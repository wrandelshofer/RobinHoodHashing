package ch.randelshofer.robinhood;

import java.util.Set;

class RobinHoodHashSetTest extends AbstractSetTest {


    @Override
    protected <T> Set<T> create(int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
        return new RobinHoodHashSet<>(initialCapacity,maxLoadFactor,growFactor,minLoadFactor);
    }

}