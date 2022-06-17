package ch.randelshofer.robinhood.guava;

import ch.randelshofer.robinhood.IdentityRobinHoodHashSet;
import com.google.common.collect.testing.MinimalCollection;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Ignore;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 Tests IdentityRobinHoodHashSet with the Guava test suite.
 <p>
 Unfortunately Guava does not test identity hash sets.
 */
public class IdentityRobinHoodHashSetGuavaTests {
    @Ignore
    public static Test suite() {
        return new IdentityRobinHoodHashSetGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite("ch.randelshofer.robinhood IdentityRobinHoodHashSet");
        suite.addTest(testsForRobinHoodHashSet());
        return suite;
    }

    public Test testsForRobinHoodHashSet() {
        return SetTestSuiteBuilder.using(
                        new TestStringSetGenerator() {
                            @Override
                            public Set<String> create(String[] elements) {
                                return new IdentityRobinHoodHashSet<>(MinimalCollection.of(elements));
                            }
                        })
                .named("IdentityRoobinHoodHashSet")
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
