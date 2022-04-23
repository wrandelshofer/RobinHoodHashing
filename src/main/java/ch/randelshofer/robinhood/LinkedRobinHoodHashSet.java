package ch.randelshofer.robinhood;


import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static ch.randelshofer.robinhood.AvalancheAlgorithms.goldenRatioAvalanche;
import static ch.randelshofer.robinhood.RangeAlgorithms.fastRange;

/**
 * Linked Robin Hood Hash Set.
 * <ul>
 *     <li>Elements are distinguished by their {@link Object#equals} method,
 *     and are hashed using their {@link Object#hashCode} method.</li>
 *     <li>Iteration order is the same as the order in which elements
 *     were added to the set.</li>
 *     <li>Does not allow null values.</li>
 * </ul>
 */
public class LinkedRobinHoodHashSet<E> extends AbstractMutableRobinHoodHashSet<E>
        implements SequencedCollection<E> {
    private transient Entry<E> first, last;
    private transient Entry<E>[] table;

    public LinkedRobinHoodHashSet() {
    }

    public LinkedRobinHoodHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedRobinHoodHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public LinkedRobinHoodHashSet(Collection<? extends E> c) {
        super(c);
    }

    public LinkedRobinHoodHashSet(Collection<? extends E> c, int initialCapacity, float loadFactor) {
        super(c, initialCapacity, loadFactor);
    }

    @Override
    protected void clearTable() {
        first = last = null;
        Arrays.fill(table, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinkedRobinHoodHashSet<E> clone() {
        try {
            LinkedRobinHoodHashSet<E> that = (LinkedRobinHoodHashSet<E>) super.clone();

            that.table = new Entry[this.table.length];
            that.first = that.last = null;
            that.size = 0;
            for (Iterator<E> it = iterator(); it.hasNext(); ) {
                that.add(it.next());
            }
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createTable(int capacity) {
        table = (Entry<E>[]) new Entry[capacity];
    }

    @Override
    public E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return first.element;
    }

    @Override
    public E getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return last.element;
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        E e = first.element;
        remove(e);
        return e;
    }

    @Override
    public E removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        E e = last.element;
        remove(e);
        return e;
    }


    @Override
    protected E getKeyFromTable(int index) {
        Entry<E> entry = table[index];
        return entry == null ? null : entry.element;
    }

    @Override
    protected int hash(Object e, int length) {
        return fastRange(goldenRatioAvalanche(Objects.hashCode(e)), length);
    }

    protected boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }

    public Iterator<E> iterator() {
        class SetIterator implements Iterator<E> {
            int mod = modCount;
            Entry<E> current = first;
            Entry<E> last = null;

            @Override
            public boolean hasNext() {
                if (mod != modCount) {
                    throw new ConcurrentModificationException();
                }
                return current != null;
            }

            @Override
            public E next() {
                if (hasNext()) {
                    E element = current.element;
                    last = current;
                    current = current.next;
                    return element;
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                if (mod != modCount) {
                    throw new ConcurrentModificationException();
                }
                if (last == null) {
                    throw new IllegalStateException();
                }
                LinkedRobinHoodHashSet.this.remove(last.element);
                last = null;
                mod = modCount;
            }
        }
        return new SetIterator();
    }

    protected void resize(int newCapacity) {
        computeThreshold(newCapacity);
        createTable(newCapacity);
        for (Entry<E> current = first; current != null; current = current.next) {
            final E o = current.element;
            int result = find(o, hash(o, newCapacity));
            var index = -result - 1;
            shiftForInsertion(index);
            table[index] = current;
        }
    }

    @Override
    protected void setKeyInTable(int index, E e) {
        Entry<E> entry = new Entry<>(e);
        if (first == null) {
            first = last = entry;
        } else {
            entry.prev = last;
            last.next = entry;
            last = entry;
        }
        table[index] = entry;
    }

    @Override
    protected void shiftForInsertion(int index) {
        shiftForInsertion1(index, table);
    }

    @Override
    protected void shiftForRemoval(int index) {
        shiftForRemoval1(index, table);
    }

    @Override
    protected void unsetTable(int index) {
        Entry<E> entry = table[index];
        if (entry.prev == null) {
            first = entry.next;
        } else {
            entry.prev.next = entry.next;
        }
        if (entry.next == null) {
            last = entry.prev;
        } else {
            entry.next.prev = entry.prev;
        }
        table[index] = null;
    }

    private static class Entry<E> {

        private final E element;
        private Entry<E> next, prev;

        private Entry(E element) {
            this.element = element;
        }

        @Override
        public String toString() {
            return "E{" +
                    element +
                    '}';
        }
    }


    /**
     * Serializes this instance.
     *
     * @serialData capacity (int),
     * load factor (float),
     * size (int),
     * all elements of the set in order.
     */
    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out all elements in the proper order.
        for (E e : this)
            s.writeObject(e);
    }

    /**
     * Deserializes this instance.
     */
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in all elements in the proper order.
        int tempSize = size;
        size = 0;
        resize(capacity);
        for (int i = 0; i < tempSize; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) s.readObject();
            add(e);
        }
    }
}
