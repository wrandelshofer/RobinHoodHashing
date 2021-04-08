package ch.randelshofer.robinhood;

import java.util.Collections;
import java.util.Set;

class LinkedRobinHoodHashMapTest extends AbstractSetTest {


    @Override
    protected <T> Set<T> create(int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
        return Collections.newSetFromMap(
                new LinkedRobinHoodHashMap<>(initialCapacity,maxLoadFactor,growFactor,minLoadFactor)
        );
    }

}