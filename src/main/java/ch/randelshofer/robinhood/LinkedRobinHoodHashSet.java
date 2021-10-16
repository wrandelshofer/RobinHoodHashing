package ch.randelshofer.robinhood;


import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static ch.randelshofer.robinhood.AvalancheAlgorithms.goldenRatioAvalanche;
import static ch.randelshofer.robinhood.RangeAlgorithms.fastRange;

/**
 * Linked Robin Hood Hash Set.
 * <ul>
 *     <li>Elements are distinguished by their {@link Object#equals} method,
 *     and are hashed using their {@link Object#hashCode} method.</li>
 *     <li>Iteration order is the same as the order in which elements
 *     were added to the set.</li>
 * </ul>
 */
public class LinkedRobinHoodHashSet<E> extends AbstractMutableRobinHoodHashSet<E> {
    private Entry<E> first, last;
    private Entry<E>[] table;

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
    public LinkedRobinHoodHashSet<E> clone() {
        LinkedRobinHoodHashSet<E> that = new LinkedRobinHoodHashSet<>(this.getCapacity(), this.getLoadFactor());
        for (Iterator<E> it = iterator(); it.hasNext(); ) {
            that.add(it.next());
        }
        return that;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createTable(int capacity) {
        table = (Entry<E>[]) new Entry[capacity];
    }


    @Override
    protected E getKeyFromTable(int index) {
        Entry<E> entry = table[index];
        return entry == null ? null : entry.element;
    }

    @Override
    protected int hash(Object e, int length) {
        return fastRange(goldenRatioAvalanche(e.hashCode()), length);
    }

    protected boolean isEqual(Object a, Object b) {
        return a.equals(b);
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
                    last=current;
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
                if (last== null) {
                    throw new IllegalStateException();
                }
                LinkedRobinHoodHashSet.this.remove(last.element);
                last=null;
                mod = modCount;
            }
        }
        return new SetIterator();
    }

    protected void resize(int newCapacity) {
        computeThreshold(newCapacity);
        createTable(newCapacity);
        for (Entry<E> current=first;current!=null;current=current.next){
            final E o = current.element;
            int result = find(o, hash(o, newCapacity));
            var index = -result - 1;
            shiftForInsertion(index);
            table[index] =current;
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
}
