package ch.randelshofer.robinhood;

import java.util.AbstractMap;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class AbstractRobinHoodHashMap<K, V> extends AbstractRobinHoodHashing<K> {
    public AbstractRobinHoodHashMap() {
    }

    public AbstractRobinHoodHashMap(int expectedSize) {
        super(expectedSize);
    }

    public AbstractRobinHoodHashMap(int expectedSize, float loadFactor) {
        super(expectedSize, loadFactor);
    }

    protected void clear() {
        if (size != 0) {
            clearTable();
            size = 0;
            modCount++;
        }
    }

    protected abstract void clearTable();

    public boolean containsKey(Object o) {
        boolean b = find(o, hash(o, capacity)) >= 0;
        return b;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            for (int i = 0; i < capacity; i++) {
                K key = getKeyFromTable(i);
                if (key != null && getValueFromTable(i) == null) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < capacity; i++) {
                V actual = getValueFromTable(i);
                if (value.equals(actual)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Map<?, ?> m)) {
            return false;
        }
        if (m.size() != size()) {
            return false;
        }

        try {
            for (ReadOnlyMapIterator it = new ReadOnlyMapIterator(); it.hasNext(); ) {
                Map.Entry<K, V> e = it.move();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key))) {
                        return false;
                    }
                } else {
                    if (!value.equals(m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException unused) {
            return false;
        }

        return true;
    }

    protected V get(Object o) {
        var h = hash(o, capacity);
        var index = find(o, h);
        if (index < 0) {
            return null;
        } else {
            return getValueFromTable(index);
        }
    }

    protected abstract V getValueFromTable(int index);

    public int hashCode() {
        int h = 0;
        for (ReadOnlyMapIterator it = new ReadOnlyMapIterator(); it.hasNext(); ) {
            h += it.move().hashCode();
        }
        return h;
    }

    protected V put(K key, V value) {
        var result = capacity == 0 ? -1 : find(key, hash(key, capacity));
        if (result < 0) {
            if (size >= threshold) {
                grow();
                result = find(key, hash(key, capacity));
            }
            var index = ~result;
            shiftForInsertion(index);
            setKeyInTable(index, key);
            setValueTable(index, value);
            size++;
            modCount++;
            return null;
        } else {
            V oldValue = getValueFromTable(result);
            setValueInTable(result, value);
            return oldValue;
        }
    }

    protected V remove(Object o) {
        var h = hash(o, capacity);
        var index = find(o, h);
        if (index < 0) {
            return null;
        } else {
            V oldValue = getValueFromTable(index);
            unsetTable(index);
            size--;
            modCount++;// must be done before shift, because debugger may advance iterator
            shiftForRemoval(index);
            return oldValue;
        }
    }

    @SuppressWarnings("unchecked")
    protected  void resize(int newCapacity) {
        Object[] objects = toArray();
        computeThreshold(newCapacity);
        createTable(newCapacity);
        for (var i = 0; i < objects.length; i += 2) {
            var o = objects[i];
            int result = find(o, hash(o, newCapacity));
            var index = -result - 1;
            shiftForInsertion(index);
            setKeyInTable(index, (K) o);
            setValueInTable(index, (V) objects[i + 1]);
        }
    }

    protected abstract void setValueInTable(int index, V value);

    protected abstract void setValueTable(int index, V value);

    protected abstract void shiftForRemoval(int index);

    protected abstract Object[] toArray();

    public String toString() {
        ReadOnlyMapIterator i = new ReadOnlyMapIterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Map.Entry<K, V> e = i.move();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    protected abstract void unsetTable(int index);

    protected class ReadOnlyMapIterator {
        int mod = modCount;
        int index = 0;
        int remaining = size;
        Map.Entry<K, V> currentEntry = null;

        public boolean hasNext() {
            if (mod != modCount) {
                throw new ConcurrentModificationException();
            }
            return remaining > 0;
        }

        protected Map.Entry<K, V> move() {
            if (hasNext()) {
                int maxIter = capacity;
                K k;
                V v;
                do {
                    k = getKeyFromTable(index);
                    v = getValueFromTable(index);
                    index = index < capacity - 1 ? index + 1 : 0;
                    --maxIter;
                } while (k == null && maxIter > 0);
                remaining--;
                currentEntry = createEntry(k, v);
                return currentEntry;
            }
            throw new NoSuchElementException();
        }

        protected Map.Entry<K, V> createEntry(K k, V v) {
            return new AbstractMap.SimpleImmutableEntry<>(k, v);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
