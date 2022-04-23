package ch.randelshofer.robinhood;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractMutableRobinHoodHashMap<K, V> extends AbstractRobinHoodHashMap<K, V> implements Map<K, V> {
    public AbstractMutableRobinHoodHashMap() {
    }

    public AbstractMutableRobinHoodHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractMutableRobinHoodHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new MutableEntrySet();
    }

    @Override
    public V get(Object key) {
        return super.get(key);
    }

    @Override
    public Set<K> keySet() {
        return new MutableKeySet();
    }

    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        return super.remove(key);
    }

    @Override
    public Collection<V> values() {
        return new MutableValueCollection();
    }

    private class MutableMapIterator extends ReadOnlyMapIterator {

        public void remove() {
            if (mod != modCount) {
                throw new ConcurrentModificationException();
            }
            if (currentEntry == null) {
                throw new IllegalStateException();
            }
            // We cannot shrink the table, because this would reorganize
            // the table.
            AbstractMutableRobinHoodHashMap.this.remove(currentEntry.getKey());
            currentEntry = null;
            mod = modCount;
        }
    }

    private class MutableKeySetIterator extends MutableMapIterator implements Iterator<K> {
        @Override
        public K next() {
            return move().getKey();
        }
    }

    private class MutableValuesIterator extends MutableMapIterator implements Iterator<V> {
        @Override
        public V next() {
            return move().getValue();
        }
    }

    private class MutableEntrySetIterator extends MutableMapIterator implements Iterator<Map.Entry<K, V>> {
        @Override
        public Map.Entry<K, V> next() {
            return move();
        }

        @Override
        protected Entry<K, V> createEntry(K k, V v) {
            return new MutableEntry(k, v);
        }
    }

    private class MutableEntry extends AbstractMap.SimpleEntry<K, V> {

        public MutableEntry(K key, V value) {
            super(key, value);
        }

        @Override
        public V setValue(V value) {
            V oldValue = super.setValue(value);
            put(getKey(), value);
            return oldValue;
        }
    }

    final class MutableKeySet extends AbstractSet<K> {
        public void clear() {
            AbstractMutableRobinHoodHashMap.this.clear();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public Iterator<K> iterator() {
            return new MutableKeySetIterator();
        }

        public boolean remove(Object key) {
            return AbstractMutableRobinHoodHashMap.this.remove(key) != null;
        }

        public int size() {
            return size;
        }
    }

    final class MutableValueCollection extends AbstractCollection<V> {
        public void clear() {
            AbstractMutableRobinHoodHashMap.this.clear();
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public Iterator<V> iterator() {
            return new MutableValuesIterator();
        }

        public int size() {
            return size;
        }
    }

    final class MutableEntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public void clear() {
            AbstractMutableRobinHoodHashMap.this.clear();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<K, V> e = (Entry<K, V>) o;
            return AbstractMutableRobinHoodHashMap.this.containsKey(e.getKey())
                    && Objects.equals(AbstractMutableRobinHoodHashMap.this.get(e.getKey()), e.getValue());
        }

        @Override
        public boolean isEmpty() {
            return AbstractMutableRobinHoodHashMap.this.isEmpty();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new MutableEntrySetIterator();
        }

        /**
         * @Override public boolean add(Entry<K, V> e) {
         * boolean added = !AbstractMutableRobinHoodHashMap.this.containsKey(e.getKey())
         * || Objects.equals(AbstractMutableRobinHoodHashMap.this.get(e.getKey()), e.getValue());
         * if (added) {
         * AbstractMutableRobinHoodHashMap.this.put(e.getKey(), e.getValue());
         * }
         * return added;
         * }
         */

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<K, V> e = (Entry<K, V>) o;
            boolean removed = AbstractMutableRobinHoodHashMap.this.containsKey(e.getKey())
                    && Objects.equals(AbstractMutableRobinHoodHashMap.this.get(e.getKey()), e.getValue());
            if (removed) {
                AbstractMutableRobinHoodHashMap.this.remove(e.getKey());
            }
            return removed;
        }

        @Override
        public int size() {
            return AbstractMutableRobinHoodHashMap.this.size();
        }

    }

}
