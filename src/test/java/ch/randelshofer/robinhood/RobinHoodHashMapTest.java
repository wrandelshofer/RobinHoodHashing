package ch.randelshofer.robinhood;

import java.util.Collections;
import java.util.Set;

class RobinHoodHashMapTest extends AbstractSetTest {


    @Override
    protected <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return Collections.newSetFromMap(new RobinHoodHashMap<>(expectedMaxSize,maxLoadFactor));
    }

}