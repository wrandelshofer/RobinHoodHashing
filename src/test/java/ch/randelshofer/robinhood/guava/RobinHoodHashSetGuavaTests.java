package ch.randelshofer.robinhood.guava;

import ch.randelshofer.robinhood.RobinHoodHashSet;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 Tests RobinHoodHashSet with the Guava test suite.
 */
public class RobinHoodHashSetGuavaTests {
    public static Test suite() {
        return new RobinHoodHashSetGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite("ch.randelshofer.robinhood RobinHoodHashSet");
        suite.addTest(testsForRobinHoodHashSet());
        return suite;
    }

    public Test testsForRobinHoodHashSet() {
        return SetTestSuiteBuilder.using(
                        new TestStringSetGenerator() {
                            @Override
                            public Set<String> create(String[] elements) {
                                return new RobinHoodHashSet<>(MinimalCollection.of(elements));
                            }
                        })
                .named("RoobinHoodHashSet")
                .withFeatures(
                        SetFeature.GENERAL_PURPOSE,
                        //CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionFeature.SERIALIZABLE,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionSize.ANY)
                .suppressing(suppressForRobinHoodHashSet())
                .createTestSuite();
    }

    protected Collection<Method> suppressForRobinHoodHashSet() {
        return Collections.emptySet();
    }
}
