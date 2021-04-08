package ch.randelshofer.robinhood;


import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Robin Hood Hash Map that keeps the iteration order the same as the order in
 * which the elements were inserted.
 */
public class LinkedRobinHoodHashMap<K,V> extends AbstractMap<K,V> implements Cloneable {
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

    private Node<K,V> first, last;

    private Node<K,V>[] table;

    private static class Node<K,V> implements Map.Entry<K,V>{
        private final int memo;
        private final K key;
        private  V value;
        private Node<K,V> next, prev;

        private Node(int memo, K key, V value) {
            this.memo = memo;
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            return null;
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
    public LinkedRobinHoodHashMap() {
        this(null, 0, 0.75f, 2f, 0.125f);
    }

    /**
     * Creates an empty hash set with the specified
     * {@code initialCapacity}, and with {@code maxLoadFactor=0.75},
     * {@code growFactor=2}, {@code minLoadFactor=0}.
     *
     * @param initialCapacity the initial capacity
     */
    public LinkedRobinHoodHashMap(int initialCapacity) {
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
    public LinkedRobinHoodHashMap(int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
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
    public LinkedRobinHoodHashMap(Map<? extends K, ? extends V> c) {
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
    public LinkedRobinHoodHashMap(Map<? extends K, ? extends V> c, int initialCapacity, float maxLoadFactor, float growFactor, float minLoadFactor) {
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
            putAll(c);
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
    public V put(K e, V v) {
        int m = memo(e);
        int result = find(e, m);
        if (result < 0) {
            if (size >= maxLoad) {
                grow();
                result = find(e, m);
            }
            int index = -result - 1;
            shiftRight(index);
            setAndLinkElement(e,v, index, m);
            size++;
            modCount++;
            return null;
        } else {
            Node<K,V> node = table[result];
            V oldValue=node.value;
            node.value=v;
            return oldValue;
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
    public LinkedRobinHoodHashMap<K,V> clone() {
        try {
            @SuppressWarnings("unchecked")
            LinkedRobinHoodHashMap<K,V> that = (LinkedRobinHoodHashMap<K,V>) super.clone();
            @SuppressWarnings("unchecked")
            Node<K,V>[] suppress =(Node<K,V>[]) new Node[this.table.length];
            that.table = suppress;
            that.first = that.last = null;
            that.size = 0;
            that.putAll(this);
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
    public boolean containsKey(Object o) {
        return find(o, memo(o)) >= 0;
    }
    @Override
    public V get(Object o) {
        int result = find(o, memo(o));
        return result >= 0 ? table[result].value : null;
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
            } else if (eekey == ekey && e.equals(getNodeKey(index))) {
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

    private K getNodeKey(int index) {
        return table[index].key;
    }

    /**
     * Gets {@code k} for the bucket {@code i}.
     */
    private int getKey(int i) {
        Node<K,V> node = table[i];
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
        Node<K,V> node = table[i];
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
    public Set<Entry<K, V>> entrySet() {
        class SetIterator implements Iterator<Entry<K, V>> {
            Node <K,V>node = first;
            final int mod = modCount;

            @Override
            public boolean hasNext() {
                if (mod != modCount) {
                    throw new ConcurrentModificationException();
                }
                return node != null;
            }

            @Override
            public Entry<K, V> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                @SuppressWarnings("unchecked")
                Node<K,V> next = node;
                node = node.next;
                return next;
            }
        }
        class EntrySet extends AbstractSet<Map.Entry<K,V>> {

            @Override
            public Iterator<Map.Entry<K,V>> iterator() {
                return new SetIterator();
            }

            @Override
            public int size() {
                return LinkedRobinHoodHashMap.this.size;
            }
        }
        return new EntrySet();
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
    public V remove(Object o) {
        int m = memo(o);
        int result = find(o, m);
        if (result < 0) {
            return null;
        } else {
            if (size <= minLoad) {
                shrink();
                result = find(o, m);
            }
            int index = result;
            Node<K,V> node=table[index];
            unlinkElement(index);
            shiftLeft(index);
            size--;
            modCount++;
            return node.value;
        }
    }

    private void resize(int desiredCapacity) {
        int newCapacity = min(desiredCapacity, Integer.MAX_VALUE - 8);
        computeMinMaxLoad(newCapacity);
        if (desiredCapacity > size && maxLoad < size) {
            throw new IllegalStateException("unable to resize");
        }

        Node<K,V>[] oldElements = table;
        @SuppressWarnings("unchecked")
        Node<K,V>[] suppress = (Node<K,V>[])new Node[newCapacity];
        this.table = suppress;

        for (Node<K,V> e : oldElements) {
            if (e != null) {
                int index = findInsertionIndex(hash(e.memo));
                shiftRight(index);
                this.table[index] = e;
            }
        }
    }

    private void setAndLinkElement(K e,V v, int index, int m) {
        Node<K,V> node = new Node<>(m, e,v);
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
        Node<K,V> node = table[index];
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
