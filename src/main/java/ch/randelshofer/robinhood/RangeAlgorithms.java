package ch.randelshofer.robinhood;

/**
 * Provides range algorithms.
 */
public class RangeAlgorithms {
    /**
     * Don't let anyone instantiate this class.
     */
    public RangeAlgorithms() {
    }

    /**
     * Maps the high-bits of a 32-bit number {@code word} into the
     * range {@code [0,p)}.
     * <p>
     * This implementation uses the FastRange algorithm by Daniel Lemire.
     * <p>
     * See Daniel Lemire (2019) in {@link ch.randelshofer.robinhood}.
     *
     * @param word a 32-bit word
     * @param p    a range, must be {@literal > 0}.
     * @return word in range {@code [0,p)}
     */
    public static int fastRange(int word, int p) {
        return (int) (((0xffffffffL & word) * p) >>> 32);
    }

    /**
     * Maps the low-bits of a 32-bit number {@code word} into the
     * range {@code [0,p)}.
     * <p>
     * This implementation uses the 'Faster Remainder by Direct Computation'
     * algorithm by Daniel Lemire, Owen Kaser, Nathan Kurz.
     * <p>
     * See Daniel Lemire, Owen Kaser, Nathan Kurz (2019) in
     * {@link ch.randelshofer.robinhood}.
     *
     * @param word a 32-bit word
     * @param p    range, must be {@literal > 0}.
     * @param invp 64-bit inverse of the range, see {@link #compute64BitInverse(int)}
     * @return word in range {@code [0,p)}
     */
    public static int fastMod(int word, int p, long invp) {
        long lowbits = invp * word;
        int mod = (int) Math.multiplyHigh(lowbits, p);
        return mod < 0 ? p + mod : mod;
    }

    /**
     * Computes the 64-bit inverse of the given value.
     * <p>
     * {@code ceil( (1<<64) / d )} = {@code Long.divideUnsigned(-1L , p) + 1}.
     * 
     * @param p a value
     * @return the 64-bit inverse 
     */
    public static long compute64BitInverse(int p) {
        return Long.divideUnsigned(-1L, p) + 1;
    }

    /**
     * Maps the low-bits of a 32-bit number {@code word} into the
     * range {@code [0,p)}.
     * <p>
     * This implementation uses the floorMod function.
     *
     * @param word a 32-bit word
     * @param p    a range, must be {@literal > 0}.
     * @return word in range {@code [0,p)}
     */
    public static int floorModRange(int word, int p) {
        return Math.floorMod(word, p);
    }

    /**
     * Maps the low-bits of a 32-bit number {@code word} into the
     * range {@code [0,p)}.
     * <p>
     * This implementation uses the modulo-operator.
     *
     * @param word a 32-bit word
     * @param p    a range, must be {@literal > 0}.
     * @return word in range {@code [0,p)}
     */
    public static int moduloRange(int word, int p) {
        return Math.abs(word % p);
    }

    /**
     * Maps the low-bits of a 32-bit number {@code word} into the
     * range {@code [0,p)}.
     * <p>
     * This implementation uses the bit-wise logical and-operator.
     *
     * @param word     a 32-bit word
     * @param powerOf2 a range, must be {@literal > 0}, must be a power of 2.
     * @return word in range {@code [0,p)}
     */
    public static int powerOf2Range(int word, int powerOf2) {
        return word & (powerOf2 - 1);
    }

    /**
     * Rounds the specified value up to a power of two, so that it
     * can be used as a length with {@link #powerOf2Range(int, int)}.
     *
     * @param value a value
     * @return the value rounded up to a power of two.
     */
    public static int roundUpToPowerOf2(int value) {
        return Math.max(0, Integer.highestOneBit(value + value - 1));
    }
}
