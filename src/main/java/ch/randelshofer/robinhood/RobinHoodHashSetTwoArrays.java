package ch.randelshofer.robinhood;


import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Robin Hood Hash Set.
 * <p>
 * A Robin Hood Hash Set is a set with an open addressing hash table, and a
 * cost-based insertion and deletion algorithm, which keeps the number of
 * probes needed to find a desired element minimal.
 * <p>
 * For each element we compute a hash value {@code h} in the range
 * {@code [0, table.length)}. {@code h} is the preferred bucket index of the
 * element.
 * <p>
 * We then compute a sort-key {@code k} based on {@code h} and the current index
 * {@code i} of the bucket in the table.
 * {@code k = (h >= i) ? h : h - table.length}.
 * The sort-key takes into account, that we have to wrap around at the end of
 * the table when we are probing for an element.
 * <p>
 * The cost {@code c} for linear probing of an element with hash code {@code h}
 * in bucket {@code i} in the table is:
 * {@code c = (h <= i) ? i - h : i - h + table.length }.
 * <p>
 * Elements with a low distance from their preferred bucket are considered to be
 * <i>'rich'</i>, elements with a high distance from their preferred bucket are
 * considered to be <i>'poor'</i>.
 * <p>
 * Invariants:
 * <ul>
 *     <li>The table always has at least one empty bucket.</li>
 *     <li>All non-empty elements in the table are sorted by their sort-key
 *     {@code k}.</li>
 *     <li>The total search lengths for all elements is minimal.</li>
 *     <li>The search lengths for all elements in the set has minimal variance.
 *     </li>
 * </ul>
 * <p>
 * References:
 * <dl>
 *     <dt>Pedro Celis (1986). Robin Hood Hashing.
 *     Data Structuring Group. Department of Computer Science.
 *     University of Waterloo. Waterloo, Ontario, N2L 3G1.</dt>
 *     <dd><a href="https://cs.uwaterloo.ca/research/tr/1986/CS-86-14.pdf">cs.waterloo.ca</a></dd>
 *
 *     <dt>Emmanuel Goossaert (2013). Robin Hood hashing: backward shift deletion.</dt>
 *     <dd><a href="https://codecapsule.com/2013/11/17/robin-hood-hashing-backward-shift-deletion/">codecapsule.com</a></dd>
 *
 *     <dt>Daniel Lemire (2019). Fast Random Integer Generation in an Interval.
 *     ACM Transactions on Modeling and Computer Simulation.
 *     January 2019. Article No. 3.</dt>
 *     <dd><a href="https://arxiv.org/pdf/1805.10941.pdf">arxiv.org</a></dd>
 *
 *     <dt>Austin Appleby (2008). MurmurHash</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/MurmurHash#Algorithm></a></a></dd>
 * </dl>
 */
public class RobinHoodHashSetTwoArrays<E> extends AbstractSet<E> implements Cloneable {
    public static final int MIN_ARRAY_SIZE = 16;
    /**
     * Factor for growing the table when the load factor would exceed
     * {@link #maxLoadFactor} upon insertion of a new element.
     * <p>
     * The inverse of this factor is used for shrinking when the load factor
     * would fall below {@link #minLoadFactor} upon deletion of an element.
     * <p>
     * This value must be {@literal > 1}.
     */
    private final float growFactor;
    /**
     * Upon deletion, when the number of non-empty elements falls below
     * this ratio, we shrink the table.
     * <p>
     * If this value is {@literal <= 0}, we never shrink the table.
     */
    private final float minLoadFactor;
    /**
     * Upon insertion, when the number of non-empty elements falls above
     * this ratio, we grow the table.
     * <p>
     * If this value is {@literal >= 1}, we never grow the table.
     */
    private final float maxLoadFactor;

    /**
     * Table with memoized hash-codes {@code m}.
     * <p>
     * This table is used to quickly check if a bucket of the table is empty,
     * and to compute {@code h} and {@code k} without having to access
     * the element.
     * <p>
     * For non-empty elements, we compute {@code m} as follows:
     * <pre>
     *     var v = murmur3(element.hashCode());
     *     var m = v == 0 ? 1_073_741_827 : v ;
     * </pre>
     * <p>
     * For an element with index {@code i}, we compute {@code h} and {@code k}
     * as follows:
     * <pre>
     *     var h = fastrange(m, table.length);
     *     var k = h < i ? table.length + h : h;
     * </pre>
     * <p>
     * Invariant:
     * <ul>
     *     <li>{@code table[i] == 0 } ⇔ {@code elements[i]} == null</li>
     * </ul>
     */
    private int[] memos;

    /**
     * Element array.
     */
    private E[] elements;

    /**
     * Modification counter for detecting concurrent modification.
     */
    private transient int modCount;

    /**
     * The number of non-empty elements in the table.
     */
    private int size;

    /**
     * Invariant: maxLoad = min(table.length * maxLoadFactor, table.length-1)
     */
    private int maxLoad;
    /**
     * Invariant: minLoad = max(table.length * minLoadFactor, 0)
     */
    private int minLoad;

    /**
     * Creates an empty hash set with
     * {@code initialCapacity=0}, {@code maxLoadFactor=0.75},
     * {@code growFactor=2}, {@code minLoadFactor=0}.
     */
    public RobinHoodHashSetTwoArrays() {
        this(null, 0, 0.75f, 2f, 0f);
    }

    /**
     * Creates an empty hash set with the specified
     * {@code initialCapacity}, and with {@code maxLoadFactor=0.75},
     * {@code growFactor=2}, {@code minLoadFactor=0}.
     *
     * @param initialCapacity the initial capacity
     */
    public RobinHoodHashSetTwoArrays(int initialCapacity) {
        this(null, initialCapacity, 0.75f, 2f, 0f);
    }

    /**
     * Creates an empty hash set with the specified constraints.
     *
     * @param initialCapacity the initial capacity
     * @param maxLoadFactor   the maximal load factor upon insertion
     * @param growFactor      the grow factor
     * @param minLoadFactor   the minimal load factor upon deletion
     */
    public RobinHoodHashSetTwoArrays(int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
        this(null, initialCapacity, maxLoadFactor, growFactor, minLoadFactor);
    }

    /**
     * Creates a hash set with the elements of the specified collection,
     * and with
     * {@code initialCapacity=c.size()*1.5}, {@code maxLoadFactor=0.9},
     * {@code growFactor=2}, {@code minLoadFactor=0}.
     *
     * @param c a collection
     */
    public RobinHoodHashSetTwoArrays(Collection<E> c) {
        this(c, (int) (c.size() * 1.5f), 0.9f, 2f, 0f);
    }

    /**
     * Creates a hash set with the elements of the specified collection,
     * and with the specified constraints.
     *
     * @param c               a collection
     * @param initialCapacity the initial capacity
     * @param maxLoadFactor   the maximal load factor upon insertion
     * @param growFactor      the grow factor
     * @param minLoadFactor   the minimal load factor upon deletion
     */
    public RobinHoodHashSetTwoArrays(Collection<E> c, int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
        if (growFactor < 1) {
            throw new IllegalArgumentException("growFactor=" + growFactor);
        }
        if (maxLoadFactor <= 0 || maxLoadFactor > 1) {
            throw new IllegalArgumentException("maxLoadFactor=" + maxLoadFactor);
        }
        if (minLoadFactor < 0 || minLoadFactor >= maxLoadFactor) {
            throw new IllegalArgumentException("minLoadFactor=" + minLoadFactor);
        }
        this.maxLoadFactor = maxLoadFactor;
        this.growFactor = growFactor;
        this.minLoadFactor = minLoadFactor;
        //noinspection unchecked
        elements = (E[]) new Object[initialCapacity];
        memos = new int[initialCapacity];
        computeMinMaxLoad(initialCapacity);
        if (c != null) {
            addAll(c);
        }
    }

    /**
     * Lemire's FastRange.
     * <p>
     * Maps a 32-bit number {@code word} into the range {@code [0,p)}.
     */
    private static int fastRange(int word, int p) {
        return (int) (((0xffffffffL & word) * p) >>> 32);
    }

    /**
     * Avalanche function re-distributes the bits of the input value.
     *
     * @param h a 32 bit value
     * @return Murmur3 value
     */
    private int avalanche(int h) {
        // Finalizer of the Murmur3 algorithm.
        h ^= h >>> MIN_ARRAY_SIZE;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> MIN_ARRAY_SIZE;
        return h;
    }

    @Override
    public boolean add(E e) {
        int m = memo(e);
        int result = find(e, m);
        if (result < 0) {
            if (size >= maxLoad) {
                grow();
                result = find(e, m);
            }
            int index = -result - 1;
            shiftRight(index);
            setElement(e, index, m);
            size++;
            modCount++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        Arrays.fill(elements, null);
        Arrays.fill(memos, 0);
        size = 0;
        shrink();
        modCount++;
    }

    @Override
    protected RobinHoodHashSetTwoArrays<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            RobinHoodHashSetTwoArrays<E> that = (RobinHoodHashSetTwoArrays<E>) super.clone();
            that.elements = this.elements.clone();
            that.memos = this.memos.clone();
            return that;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    private void computeMinMaxLoad(int capacity) {
        minLoad = (int) (capacity * minLoadFactor);
        maxLoad = (int) Math.max(0, Math.min(capacity - 1, capacity * maxLoadFactor));
    }

    @Override
    public boolean contains(Object o) {
        return find(o, memo(o)) >= 0;
    }

    String dump() {
        StringBuilder b = new StringBuilder();
        b.append("{\n");
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] == null) {

            } else {
                int h = hash(elements[i]);
                int cost = getCost(i);// h > i ? i - h + elements.length : i - h;
                b.append("  a[").append(i).append("]=").append(elements[i])
                        .append(" hash=").append(h)
                        .append(" memo=").append(memos[i])
                        .append(" key=").append(getKey(i))
                        .append(" cost=")
                        .append(cost);
            }
            b.append('\n');
        }
        b.append("}\n");
        return b.toString();
    }
  void dumpStats() {
        IntSummaryStatistics stats=new IntSummaryStatistics();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] == null) {

            } else {
                int h = hash(elements[i]);
                int cost = getCost(i);// h > i ? i - h + elements.length : i - h;
                stats.accept(cost);
            }
        }
        System.out.println("cost "+stats);
      System.out.println("capacity "+elements.length);
    }

    /**
     * Searches for the specified element.
     * <p>
     * If the element is present, returns the index of the
     * bucket that contains the element.
     * <p>
     * If the element is absent returns {@code (-index - 1)} where
     * {@code index} is the last unsuccessfully probed index
     * in the array.
     */
    private int find(Object desired, int m) {
        if (memos.length==0)return -1;
        int desiredHash = hash(m);
        int index = desiredHash;
        int desiredKey = desiredHash;

        while (true) {
            int candidateKey = getKey(index);
            if (candidateKey > desiredKey) {
                return -index - 1;
            } else if (candidateKey == desiredKey && desired.equals(getElement(index))) {
                return index;
            } else {
                if (++index == elements.length) {
                    index = 0;
                    desiredKey = desiredKey - elements.length;
                }
            }
        }
    }

    private int findInsertionIndex(int ehash) {
        if (elements.length == 0) {
            return -1;
        }

        int index = ehash;
        int ekey = ehash;

        while (true) {
            int eekey = getKey(index);
            if (eekey > ekey) {
                return index;
            } else {
                if (++index == elements.length) {
                    index = 0;
                    ekey = ekey - elements.length;
                }
            }
        }
    }

    private E getElement(int index) {
        return elements[index];
    }

    /**
     * Gets {@code k} for the bucket {@code i}.
     */
    private int getKey(int i) {
        int m = memos[i];
        if (m == 0) {
            return Integer.MAX_VALUE;
        }
        int h = fastRange(m, memos.length);
        return h <= i ? h : h - memos.length;
    }

    /**
     * Gets {@code h} for the bucket {@code i}.
     */
    private int getHash(int i) {
        int m = memos[i];
        return fastRange(m, memos.length);
    }

    /**
     * Gets {@code c} for the bucket {@code i}.
     */
    private int getCost(int i) {
        int m = memos[i];
        if (m == 0) {
            return 0;
        }
        int h = hash(m);
        return (h <= i) ? i - h : i - h + memos.length;
    }

    private void grow() {
        long desiredCapacity = min(Integer.MAX_VALUE, max((long) (elements.length * growFactor), MIN_ARRAY_SIZE));
        if (desiredCapacity > elements.length) {
            resize((int) desiredCapacity);
        }
    }

    /**
     * Computes an index for {@code e} in the range {@code [0, length)}.
     * <p>
     * Java hash values often have the highest variance in the low-order
     * bits. We re-distribute the bits with an avalanche function and
     * then map the resulting 32-bit value into the output range.
     *
     * @param e an element
     * @return an index
     */
    private int hash(Object e) {
        int m = memo(e);
        return fastRange(m, memos.length);
    }

    private int hash(int m) {
        return fastRange(m, memos.length);
    }

    @Override
    public Iterator<E> iterator() {
        class SetIterator implements Iterator {
            final int mod = modCount;
            int index = 0;
            int count = 0;

            @Override
            public boolean hasNext() {
                if (mod != modCount) {
                    throw new ConcurrentModificationException();
                }
                return count < size && index < elements.length;
            }

            @Override
            public E next() {
                if (hasNext()) {
                    E e;
                    do {
                        e = elements[index++];
                    } while (e == null);
                    count++;
                    return e;
                }
                throw new NoSuchElementException();
            }
        }
        return new SetIterator();
    }

    /**
     * Computes {@code m} for the specified element.
     *
     * @param e an element
     * @return {@code m} with {@code m != 0}.
     */
    private int memo(Object e) {
        int ava = avalanche(e.hashCode());
        return ava == 0 ? 1_073_741_827 : ava;
    }

    @Override
    public boolean remove(Object o) {
        int m = memo(o);
        int result = find(o, m);
        if (result < 0) {
            return false;
        } else {
            if (size <= minLoad) {
                shrink();
                result = find(o, m);
            }
            int index = result;
            shiftLeft(index);
            size--;
            modCount++;
            return true;
        }
    }

    private void resize(int desiredCapacity) {
        int newCapacity = (int) min(desiredCapacity, Integer.MAX_VALUE - 8);
        computeMinMaxLoad(newCapacity);
        if (desiredCapacity > size && maxLoad < size) {
            throw new IllegalStateException("unable to resize");
        }

        E[] oldElements = elements;
        //noinspection unchecked
        elements = (E[]) new Object[newCapacity];

        int[] oldMemos = memos;
        memos = new int[newCapacity];

        for (int i = 0; i < oldElements.length; i++) {
            E e = oldElements[i];
            if (e != null) {
                int index = findInsertionIndex(hash(oldMemos[i]));
                shiftRight(index);
                setElement(e, index, oldMemos[i]);
            }
        }
    }

    private void setElement(E e, int index, int m) {
        elements[index] = e;
        memos[index] = m;
    }

    private void shiftLeft(int index) {
        if (memos[index] == 0) {
            return;
        }

        int length = memos.length;
        int end = index + 1 == length ? 0 : index + 1;
        while (getCost(end) != 0) {
            if (++end == length) {
                end = 0;
            }
        }

        if (end < index) {
            // wrap around
            System.arraycopy(elements, index + 1, elements, index, length - index - 1);
            System.arraycopy(memos, index + 1, memos, index, length - index - 1);
            elements[length - 1] = elements[0];
            memos[length - 1] = memos[0];
            if (end > 0) {
                System.arraycopy(elements, 1, elements, 0, end - 1);
                System.arraycopy(memos, 1, memos, 0, end - 1);
            }
        } else {
            System.arraycopy(elements, index + 1, elements, index, end - index);
            System.arraycopy(memos, index + 1, memos, index, end - index);
        }

        unsetElement(end == 0 ? elements.length - 1 : end - 1);
    }

    private void shiftRight(int index) {
        if (memos[index] == 0) {
            return;
        }

        int length = memos.length;
        int end = index;
        while (memos[end] != 0) {
            if (++end == length) {
                end = 0;
            }
        }

        if (end < index) {
            // wrap around
            System.arraycopy(elements, 0, elements, 1, end);
            System.arraycopy(memos, 0, memos, 1, end);
            elements[0] = elements[length - 1];
            memos[0] = memos[length - 1];
            System.arraycopy(elements, index, elements, index + 1, length - index - 1);
            System.arraycopy(memos, index, memos, index + 1, length - index - 1);
        } else {
            System.arraycopy(elements, index, elements, index + 1, end - index);
            System.arraycopy(memos, index, memos, index + 1, end - index);
        }

        unsetElement(index);
    }

    private void shrink() {
        long desiredCapacity = max((long) (elements.length * (maxLoadFactor + minLoadFactor) * 0.5), MIN_ARRAY_SIZE);
        if (desiredCapacity < elements.length) {
            resize((int) desiredCapacity);
        }
    }

    @Override
    public int size() {
        return size;
    }

    private void unsetElement(int index) {
        elements[index] = null;
        memos[index] = 0;
    }
}
