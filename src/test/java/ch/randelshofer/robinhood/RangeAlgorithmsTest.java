package ch.randelshofer.robinhood;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RangeAlgorithmsTest {
    @TestFactory
    public List<DynamicTest> fastModuloShouldComputeModuloForPositiveValues() {
        return List.of(
                dynamicTest("1,1", () -> testFastModulo(1, 1)),
                dynamicTest("1,max", () -> testFastModulo(1, Integer.MAX_VALUE)),
                dynamicTest("max-1,max", () -> testFastModulo(Integer.MAX_VALUE - 1, Integer.MAX_VALUE))
        );
    }

    private void testFastModulo(int v, int p) {
        int expected = RangeAlgorithms.moduloRange(v, p);
        long invp = RangeAlgorithms.compute64BitInverse(p);
        int actual = RangeAlgorithms.fastMod(v, p, invp);
        assertEquals(expected, actual);
    }
}
