package ch.randelshofer.robinhood;

import java.io.Serializable;
import java.util.IntSummaryStatistics;

public abstract class AbstractRobinHoodHashing<E> implements Cloneable, Serializable {
    /**
     * The number of non-empty elements in the table.
     */
    protected int size;
    /**
     * Upon insertion, when the number of non-empty elements falls above
     * this ratio, we grow the table.
     * <p>
     * Robert Sedgewick, Kevin Wayne (2011) propose to never fill an
     * open addressing hash table with linear probing by more than 50%.
     * See {@link ch.randelshofer.robinhood}.
     * <p>
     * If this value is {@literal >= 1} the table will never grow.
     */
    protected float loadFactor;

    /**
     * Modification counter for detecting concurrent modification.
     */
    protected transient int modCount;

    /**
     * Invariant: maxLoad = clamp(table.length * maxLoadFactor, 0, table.length)
     */
    protected int threshold;

    /**
     * The capacity of the hash table.
     */
    protected int capacity;


    protected AbstractRobinHoodHashing() {
        this(0, 0.5f);
    }


    protected AbstractRobinHoodHashing(int initialCapacity) {
        this(initialCapacity, 0.5f);
    }

    /**
     * Creates an empty hash set with the specified constraints.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the maximal load factor upon insertion
     */
    protected AbstractRobinHoodHashing(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity=" + initialCapacity);
        }
        if (loadFactor <= 0 || loadFactor > 1) {
            throw new IllegalArgumentException("loadFactor=" + loadFactor);
        }
        this.loadFactor = loadFactor;
        this.capacity = roundCapacity(initialCapacity);
        computeThreshold(this.capacity);
        createTable(this.capacity);
    }

    protected void computeThreshold(int capacity) {
        threshold = Math.max((int) (capacity * loadFactor), 0);
    }

    /**
     * Rounds the capacity up so that it supports the range operation.
     */
    protected int roundCapacity(int desiredCapacity) {
        return desiredCapacity;
    }

    protected abstract void createTable(int capacity);


    public float getLoadFactor() {
        return loadFactor;
    }

    public int size() {
        return size;
    }


    public boolean isEmpty() {
        return size == 0;
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
     *
     * @param expected the object to be found
     * @param h        the hash of the object to be found
     */
    protected int find(Object expected, int h) {
        if (capacity == 0) {
            return -1;
        }
        var index = h;
        var expectedKey = h;

        while (true) {
            var actual = getKeyFromTable(index);
            if (actual == null) {
                return -index - 1;
            }
            if (isEqual(expected, actual)) {
                return index;
            }
            var actualKey = getHashKey(actual, index);
            if (actualKey > expectedKey) {
                return -index - 1;
            } else {
                if (++index == capacity) {
                    index = 0;
                    expectedKey = expectedKey - capacity;
                }
            }
        }
    }

    protected abstract E getKeyFromTable(int index);

    protected int getHashKey(Object e, int i) {
        if (e == null) {
            return Integer.MAX_VALUE;
        }
        var h = hash(e, capacity);
        return h <= i ? h : h - capacity;
    }

    /**
     * Computes a hash code for {@code e} in the range {@code [0, length)}.
     *
     * @param e      an element
     * @param length the table length
     * @return an index
     */
    protected abstract int hash(Object e, int length);

    /**
     * Returns true if the provided objects are equal
     *
     * @param a object a
     * @param b object b
     * @return whether the objects are equal
     */
    protected abstract boolean isEqual(Object a, Object b);

    /**
     * Gets {@code c} for the entry in the table of length {@code length}
     * at index {@code i}.
     */
    protected int getCost(int i) {
        var entry = getKeyFromTable(i);
        if (entry == null) {
            return 0;
        }
        var h = hash(entry, capacity);
        return (h <= i) ? i - h : i - h + capacity;
    }

    public int getCapacity() {
        return capacity;
    }


    public float getFillRatio() {
        return size / (float) capacity;
    }

    public IntSummaryStatistics getCostStatistics() {
        var stats = new IntSummaryStatistics();
        for (var i = 0; i < capacity; i++) {
            if (getKeyFromTable(i) != null) {
                stats.accept(getCost(i));
            }
        }
        return stats;
    }

    protected void shiftForRemoval1(int index, Object[] table) {


        // Find length of elements to shift.
        // Here we rely on the fact that there is always at least one
        // table bucket with zero cost in the table. This is guaranteed
        // by the invariant that there is always at least one empty bucket
        // in the table.
        var length = table.length;
        var end = index + 1 == length ? 0 : index + 1;
        while (getCost(end) != 0) {
            if (++end == length) {
                end = 0;
            }
        }

        if (end == index + 1) {

        } else if (end < index) {
            // wrap around
            System.arraycopy(table, index + 1, table, index, length - index - 1);
            table[length - 1] = table[0];
            if (end > 0) {
                System.arraycopy(table, 1, table, 0, end - 1);
            }
        } else {
            System.arraycopy(table, index + 1, table, index, end - index);
        }

        var index1 = end == 0 ? table.length - 1 : end - 1;
        table[index1] = null;
    }

    protected void shiftForRemoval2(int index, Object[] table) {


        // Find length of elements to shift.
        // Here we rely on the fact that there is always at least one
        // table bucket with zero cost in the table. This is guaranteed
        // by the invariant that there is always at least one empty bucket
        // in the table.
        var length = table.length / 2;
        var end = index + 1 == length ? 0 : index + 1;
        while (getCost(end) != 0) {
            if (++end == length) {
                end = 0;
            }
        }
        if (end == index + 1) {
        } else if (end < index) {
            // wrap around
            System.arraycopy(table, (index + 1) * 2, table, index * 2, (length - index - 1) * 2);
            table[length - 1] = table[0];
            if (end > 0) {
                System.arraycopy(table, 2, table, 0, (end - 1) * 2);
            }
        } else {
            System.arraycopy(table, (index + 1) * 2, table, index * 2, (end - index) * 2);
        }

        var index1 = end == 0 ? length - 1 : end - 1;
        table[index1 * 2] = null;
        table[index1 * 2 + 1] = null;
    }


    protected void shiftForInsertion1(int index, Object[] table) {
        if (table[index] == null) {
            return;
        }

        var length = table.length;
        var end = index < length - 1 ? index + 1 : 0;
        while (table[end] != null) {
            if (++end == length) {
                end = 0;
            }
        }

        if (end < index) {
            // wrap around
            System.arraycopy(table, 0, table, 1, end);
            table[0] = table[length - 1];
            System.arraycopy(table, index, table, index + 1, length - index - 1);
        } else {
            System.arraycopy(table, index, table, index + 1, end - index);
        }

        table[index] = null;
    }

    protected void shiftForInsertion2(int index, Object[] table) {
        if (table[index * 2] == null) {
            return;
        }

        var length = table.length / 2;
        var end = index < length - 1 ? index + 1 : 0;
        while (table[end * 2] != null) {
            if (++end == length) {
                end = 0;
            }
        }

        if (end < index) {
            // wrap around
            System.arraycopy(table, 0, table, 2, end * 2);
            table[0] = table[(length - 1) * 2];
            table[1] = table[(length - 1) * 2 + 1];
            System.arraycopy(table, index * 2, table, (index + 1) * 2, (length - index - 1) * 2);
        } else {
            System.arraycopy(table, index * 2, table, (index + 1) * 2, (end - index) * 2);
        }

        table[index * 2] = null;
        table[index * 2 + 1] = null;
    }

    @SuppressWarnings("unchecked")
    protected abstract void resize(int newCapacity);


    protected void grow() {
        int desiredCapacity = Math.max(1, capacity  *2);
        if (desiredCapacity < size + 1) {
            throw new IllegalStateException("Cannot grow table.");
        }
        if (desiredCapacity > capacity) {
            capacity = desiredCapacity;
            resize(desiredCapacity);
        }
    }

    protected abstract void shiftForInsertion(int index);

    protected abstract void setKeyInTable(int index, E e);



}
