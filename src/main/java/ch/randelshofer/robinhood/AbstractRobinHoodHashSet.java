package ch.randelshofer.robinhood;


import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import static java.lang.Math.max;

/**
 * Robin Hood Hash Set.
 */
abstract class AbstractRobinHoodHashSet<E> extends AbstractRobinHoodHashing<E> implements Cloneable, Iterable<E> {
    public AbstractRobinHoodHashSet() {
        super();
    }

    public AbstractRobinHoodHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractRobinHoodHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    protected boolean add(E e) {
        var result = capacity == 0 ? -1 : find(e, hash(e, capacity));
        if (result < 0) {
            if (size >= threshold) {
                grow();
                result = find(e, hash(e, capacity));
            }
            var index = -result - 1;
            shiftForInsertion(index);
            setKeyInTable(index, e);
            size++;
            modCount++;
            return true;
        } else {
            return false;
        }
    }

    protected void clear() {
        clearTable();
        size = 0;
        modCount++;
    }

    protected abstract void clearTable();

    @Override
    protected AbstractRobinHoodHashSet<E> clone() {
        @SuppressWarnings("unchecked")
        var that = (AbstractRobinHoodHashSet<E>) super.clone();
        return that;
    }


    public boolean contains(Object o) {
        boolean b = find(o, hash(o, capacity)) >= 0;
        return b;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }


    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Set)) {
            return false;
        }
        var c = (Collection<?>) o;
        if (c.size() != size()) {
            return false;
        }
        try {
            return containsAll(c);
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }


    /**
     * Gets {@code k} for the bucket {@code i}
     * in the table of length {@code length}.
     */
    private int getKey(int i) {
        var entry = getKeyFromTable(i);
        if (entry == null) {
            return Integer.MAX_VALUE;
        }
        var h = hash(entry, capacity);
        return h <= i ? h : h - capacity;
    }


    public int hashCode() {
        var h = 0;
        for (var e : this) {
            if (e != null) {
                h += e.hashCode();
            }
        }
        return h;
    }

    @SuppressWarnings("unchecked")
    protected void resize(int newCapacity) {
        Object[] objects = toArray();
        computeThreshold(newCapacity);
        createTable(newCapacity);
        for (Object o : objects) {
            @SuppressWarnings("RedundantExplicitVariableType") E e = (E) o;
            int result = find(o, hash(o, newCapacity));
            var index = -result - 1;
            shiftForInsertion(index);
            setKeyInTable(index, e);
        }
    }


    public Iterator<E> iterator() {
        class SetIterator implements Iterator<E> {
            int mod = modCount;
            int index = 0;
            int remaining = size;
            E current = null;

            @Override
            public boolean hasNext() {
                if (mod != modCount) {
                    throw new ConcurrentModificationException();
                }
                return remaining > 0;
            }

            @Override
            public E next() {
                if (hasNext()) {
                    int maxIter = capacity;
                    do {
                        current = getKeyFromTable(index);
                        index = index < capacity - 1 ? index + 1 : 0;
                        --maxIter;
                    } while (current == null && maxIter > 0);
                    remaining--;
                    return current;
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                if (mod != modCount) {
                    throw new ConcurrentModificationException();
                }
                if (current == null) {
                    throw new IllegalStateException();
                }
                // We cannot shrink the table, because this would reorganize
                // the table.
                AbstractRobinHoodHashSet.this.remove(current);
                current = null;
                mod = modCount;
            }
        }
        return new SetIterator();
    }

    /**
     * Removes the specified object if it is contained in the set.
     *
     * @param o an object
     * @return whether the object was in the set
     */
    protected boolean remove(Object o) {
        var h = hash(o, capacity);
        var result = find(o, h);
        if (result < 0) {
            return false;
        } else {
            var index = result;
            unsetTable(index);
            size--;
            modCount++;// must be done before shift, because debugger may advance iterator
            shiftForRemoval(index);
            return true;
        }
    }


    protected abstract void shiftForRemoval(int index);


    public void sizeToFit(float fillRatio) {
        if (size == 0) {
            resize(0);
        } else {
            resize(max((int) (size / fillRatio), size));
        }
    }

    public Spliterator<E> spliterator() {
        class SetSpliterator extends Spliterators.AbstractSpliterator<E> {
            private final int fence;
            private final int expectedModCount;
            private int index;

            protected SetSpliterator(int index, int fence) {
                super(fence - index, DISTINCT);
                this.index = index;
                this.fence = fence;
                this.expectedModCount = modCount;
            }

            @Override
            public long estimateSize() {
                return fence - index;
            }

            @Override
            public boolean tryAdvance(Consumer<? super E> action) {
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                while (index < fence && getKeyFromTable(index) == null) {
                    index++;
                }
                if (index < fence) {
                    action.accept(getKeyFromTable(index++));
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<E> trySplit() {
                int lo = index, mid = (lo + fence) >>> 1;
                return (lo >= mid)
                        ? null
                        : new SetSpliterator(lo, index = mid);
            }
        }
        return new SetSpliterator(0, capacity);
    }

    public Object[] toArray() {
        var r = new Object[size()];
        var it = iterator();
        for (var i = 0; i < r.length; i++) {
            r[i] = it.next();
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        var size = size();
        var r = a.length >= size ? a :
                (T[]) java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), size);
        var it = iterator();
        for (var i = 0; i < size; i++) {
            r[i] = (T) it.next();
        }
        return r;
    }

    public String toString() {
        var sb = new StringBuilder();
        sb.append('[');
        for (var e : this) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(e == this ? "(this Collection)" : e);
        }
        return sb.append(']').toString();
    }

    protected abstract void unsetTable(int index);
}
