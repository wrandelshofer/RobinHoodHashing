package ch.randelshofer.robinhood;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class AbstractMutableRobinHoodHashMap<K, V> extends AbstractRobinHoodHashMap<K, V> implements Map<K, V> {
    private class MapIterator {
        int mod = modCount;
        int index = 0;
        int remaining = size;
        K currentKey = null;

        public boolean hasNext() {
            if (mod != modCount) {
                throw new ConcurrentModificationException();
            }
            return remaining > 0;
        }

        protected K move() {
            if (hasNext()) {
                int maxIter = capacity;
                do {
                    currentKey = getKeyFromTable(index);
                    index = index < capacity - 1 ? index + 1 : 0;
                    --maxIter;
                } while (currentKey == null && maxIter > 0);
                remaining--;
                return currentKey;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (mod != modCount) {
                throw new ConcurrentModificationException();
            }
            if (currentKey == null) {
                throw new IllegalStateException();
            }
            // We cannot shrink the table, because this would reorganize
            // the table.
            AbstractMutableRobinHoodHashMap.this.remove(currentKey);
            currentKey = null;
            mod = modCount;
        }
    }

    private class KeySetIterator extends MapIterator implements Iterator<K> {
        @Override
        public K next() {
            return move();
        }
    }

    public AbstractMutableRobinHoodHashMap() {
    }

    public AbstractMutableRobinHoodHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractMutableRobinHoodHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    private Set<K> keySet = null;

    final class KeySet extends AbstractSet<K> {
        public final int size() {
            return size;
        }

        public final void clear() {
            AbstractMutableRobinHoodHashMap.this.clear();
        }

        public final Iterator<K> iterator() {
            return new KeySetIterator();
        }

        public final boolean contains(Object o) {
            return containsKey(o);
        }

        public final boolean remove(Object key) {
            return AbstractMutableRobinHoodHashMap.this.remove(key) != null;
        }

        public final Spliterator<K> spliterator() {
            throw new UnsupportedOperationException();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        public final void forEach(Consumer<? super K> action) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    public V remove(Object key) {
        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }


    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }


    @Override
    protected AbstractMutableRobinHoodHashMap clone() {
        @SuppressWarnings("unchecked")
        var that = (AbstractMutableRobinHoodHashMap) super.clone();
        that.keySet = null;
        return that;
    }

}
