package ch.randelshofer.robinhood.guava;

import ch.randelshofer.robinhood.RobinHoodHashMap;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 Tests RobinHoodHashSet with the Guava test suite.
 */
public class RobinHoodHashMapGuavaTests {
    public static Test suite() {
        return new RobinHoodHashMapGuavaTests().allTests();
    }

    public Test allTests() {
        TestSuite suite = new TestSuite("ch.randelshofer.robinhood RobinHoodHashMap");
        suite.addTest(testsForRobinHoodHashMap());
        return suite;
    }

    public Test testsForRobinHoodHashMap() {
        return MapTestSuiteBuilder.using(
                        new TestStringMapGenerator() {
                            @Override
                            protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                                return toHashMap(entries);
                            }
                        })
                .named("RobinHoodHashMap")
                .withFeatures(
                        MapFeature.GENERAL_PURPOSE,
                        //MapFeature.ALLOWS_NULL_KEYS,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.ALLOWS_ANY_NULL_QUERIES,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        CollectionFeature.SERIALIZABLE,
                        CollectionSize.ANY)
                .suppressing(suppressForRobinHoodHashMap())
                .createTestSuite();
    }

    protected Collection<Method> suppressForRobinHoodHashMap() {
        return Collections.emptySet();
    }


    private static Map<String, String> toHashMap(Map.Entry<String, String>[] entries) {
        return new RobinHoodHashMap<String, String>(Arrays.asList(entries));
    }
}
