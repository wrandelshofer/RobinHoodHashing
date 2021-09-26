package ch.randelshofer.robinhood;

import java.util.Set;

class IdentityRobinHoodHashSetTest extends AbstractSetTest {


    @Override
    protected <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new IdentityRobinHoodHashSet<>(expectedMaxSize,maxLoadFactor);
    }

}