package ch.randelshofer.robinhood;


import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Robin Hood Hash Set that keeps the iteration order the same as the order in
 * which the elements were inserted.
 */
public class LinkedRobinHoodHashSet<E> extends AbstractSet<E> implements Cloneable {
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

    private Node<E> first, last;

    private Node<E>[] table;

    private static class Node<E> {
        private final int memo;
        private final E element;
        private Node<E> next, prev;

        private Node(int memo, E element) {
            this.memo = memo;
            this.element = element;
        }
    }


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
    public LinkedRobinHoodHashSet() {
        this(null, 0, 0.75f, 2f, 0.125f);
    }

    /**
     * Creates an empty hash set with the specified
     * {@code initialCapacity}, and with {@code maxLoadFactor=0.75},
     * {@code growFactor=2}, {@code minLoadFactor=0}.
     *
     * @param initialCapacity the initial capacity
     */
    public LinkedRobinHoodHashSet(int initialCapacity) {
        this(null, initialCapacity, 0.75f, 2f, 0.125f);
    }

    /**
     * Creates an empty hash set with the specified constraints.
     *
     * @param initialCapacity the initial capacity
     * @param maxLoadFactor   the maximal load factor upon insertion
     * @param growFactor      the grow factor
     * @param minLoadFactor   the minimal load factor upon deletion
     */
    public LinkedRobinHoodHashSet(int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
        this(null, initialCapacity, maxLoadFactor, growFactor, minLoadFactor);
    }

    /**
     * Creates a hash set with the elements of the specified collection,
     * and with
     * {@code initialCapacity=c.size()*1.5}, {@code maxLoadFactor=0.75},
     * {@code growFactor=2}, {@code minLoadFactor=0}.
     *
     * @param c a collection
     */
    public LinkedRobinHoodHashSet(Collection<? extends E> c) {
        this(c, (int) (c.size() * 1.5f), 0.75f, 2f, 0.125f);
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
    public LinkedRobinHoodHashSet(Collection<? extends E> c, int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
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
        table = new Node[initialCapacity];
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
     * <p>
     * Because the table uses fastRange for mapping hashCodes, sets of hashes
     * that vary only in low order bits will always collide.
     * <p>
     * We apply the last stage of murmur3 to the hash code.
     *
     * @param key an object that we want to lookup in the hash table
     * @return avalanche value
     */
    private int avalanche(Object key) {
        int h = key.hashCode();
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
            setAndLinkElement(e, index, m);
            size++;
            modCount++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        Arrays.fill(table, null);
        size = 0;
        first = last = null;
        shrink();
        modCount++;
    }

    @Override
    public LinkedRobinHoodHashSet<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            LinkedRobinHoodHashSet<E> that = (LinkedRobinHoodHashSet<E>) super.clone();
            @SuppressWarnings("unchecked")
            Node<E>[] suppress = new Node[this.table.length];
            that.table = suppress;
            that.first = that.last = null;
            that.size = 0;
            that.addAll(this);
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
    private int find(Object e, int m) {
        if (table.length == 0) {
            return -1;
        }
        int ehash = hash(m);
        int index = ehash;
        int ekey = ehash;

        while (true) {
            int eekey = getKey(index);
            if (eekey > ekey) {
                return -index - 1;
            } else if (eekey == ekey && e.equals(getElement(index))) {
                return index;
            } else {
                if (++index == table.length) {
                    index = 0;
                    ekey = ekey - table.length;
                }
            }
        }
    }

    private int findInsertionIndex(int ehash) {
        if (table.length == 0) {
            return -1;
        }

        int index = ehash;
        int ekey = ehash;

        while (true) {
            int eekey = getKey(index);
            if (eekey > ekey) {
                return index;
            } else {
                if (++index == table.length) {
                    index = 0;
                    ekey = ekey - table.length;
                }
            }
        }
    }

    private E getElement(int index) {
        E element = table[index].element;
        return element;
    }

    /**
     * Gets {@code k} for the bucket {@code i}.
     */
    private int getKey(int i) {
        Node<E> node = table[i];
        if (node == null) {
            return Integer.MAX_VALUE;
        }
        int m = node.memo;
        int h = fastRange(m, table.length);
        return h <= i ? h : h - table.length;
    }

    /**
     * Gets {@code c} for the bucket {@code i}.
     */
    private int getCost(int i) {
        Node<E> node = table[i];
        if (node == null) {
            return 0;
        }
        int h = hash(node.memo);
        return (h <= i) ? i - h : i - h + table.length;
    }

    private void grow() {
        long desiredCapacity = min(Integer.MAX_VALUE, max((long) (table.length * growFactor), MIN_ARRAY_SIZE));
        if (desiredCapacity > table.length) {
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
     * @param m a hash code that was treated with {@link #avalanche(Object)}
     * @return an index
     */
    private int hash(int m) {
        return fastRange(m, table.length);
    }

    @Override
    public Iterator<E> iterator() {
        class SetIterator implements Iterator<E> {
            Node <E>node = first;
            final int mod = modCount;

            @Override
            public boolean hasNext() {
                if (mod != modCount) {
                    throw new ConcurrentModificationException();
                }
                return node != null;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                E next = node.element;
                node = node.next;
                return next;
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
        return avalanche(e.hashCode());
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
            unlinkElement(index);
            shiftLeft(index);
            size--;
            modCount++;
            return true;
        }
    }

    private void resize(int desiredCapacity) {
        int newCapacity = min(desiredCapacity, Integer.MAX_VALUE - 8);
        computeMinMaxLoad(newCapacity);
        if (desiredCapacity > size && maxLoad < size) {
            throw new IllegalStateException("unable to resize");
        }

        Node<E>[] oldElements = table;
        @SuppressWarnings("unchecked")
        Node<E>[] suppress =(Node<E>[]) new Node[newCapacity];
        this.table = suppress;

        for (Node<E> e : oldElements) {
            if (e != null) {
                int index = findInsertionIndex(hash(e.memo));
                shiftRight(index);
                this.table[index] = e;
            }
        }
    }

    private void setAndLinkElement(E e, int index, int m) {
        Node<E> node = new Node<>(m, e);
        table[index] = node;
        if (first == null) {
            first = last = node;
        } else {
            node.prev = last;
            last.next = node;
            last = node;
        }
    }

    private void shiftLeft(int index) {
        if (table[index] == null) {
            return;
        }

        int length = table.length;
        int end = index + 1 == length ? 0 : index + 1;
        while (getCost(end) != 0) {
            if (++end == length) {
                end = 0;
            }
        }

        if (end < index) {
            // wrap around
            System.arraycopy(table, index + 1, table, index, length - index - 1);
            table[length - 1] = table[0];
            if (end > 0) {
                System.arraycopy(table, 1, table, 0, end - 1);
            }
        } else {
            System.arraycopy(table, index + 1, table, index, end - index);
        }

        table[end == 0 ? table.length - 1 : end - 1] = null;
    }

    private void shiftRight(int index) {
        if (table[index] == null) {
            return;
        }

        int length = table.length;
        int end = index;
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

    private void shrink() {
        long desiredCapacity = max((long) (table.length * (maxLoadFactor + minLoadFactor) * 0.5), MIN_ARRAY_SIZE);
        if (desiredCapacity < table.length) {
            resize((int) desiredCapacity);
        }
    }

    @Override
    public int size() {
        return size;
    }

    private void unlinkElement(int index) {
        Node<E> node = table[index];
        //table[index] = null;
        if (node.prev == null) {
            first = node.next;
        } else {
            node.prev.next = node.next;
        }
        if (node.next == null) {
            last = node.prev;
        } else {
            node.next.prev = node.prev;
        }
    }
}
