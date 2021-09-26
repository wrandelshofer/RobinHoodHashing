package ch.randelshofer.robinhood;

/** Provides avalanche algorithms. */
public class AvalancheAlgorithms {
    /** Don't let anyone instantiate this class. */
    public AvalancheAlgorithms() {
    }

    /**
     * Avalanches the bits of an integer by applying the finalization
     * step of the Murmur3 algorithm.
     * <p>
     * Reference: Austin Appleby (2008) in {@link ch.randelshofer.robinhood}.
     *
     * @param h a 32-bit integer
     * @return avalanche value
     */
    public static int murmur3Avalanche(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    /**
     * Avalanches the bits of an integer by multiplying by the golden ratio and
     * xor-shifting the result.
     * <p>
     * It has slightly worse behaviour than {@link #murmur3Avalanche(int)},
     * but it is much faster.
     * <pre>
     * golden ratio = ɸ = (√5 - 1)/2.
     * phi32 = 2³² ﹒ɸ = 2654435769.497 = 2654435769L = 0x9E3779B9;
     * </pre>
     * Reference: Sebastiano Vigna (2002-2021) in {@link ch.randelshofer.robinhood}.
     *
     * @param x a 32-bit integer
     * @return avalanche value
     */
    public static int goldenRatioAvalanche(int x) {
        var h = x * 0x9E3779B9;
        return h ^ (h >>> 16);
    }
}
