package ch.randelshofer.robinhood;

public abstract class AbstractRobinHoodHashMap<K, V> extends AbstractRobinHoodHashing<K> {
    public AbstractRobinHoodHashMap() {
    }

    public AbstractRobinHoodHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractRobinHoodHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public boolean containsKey(Object o) {
        boolean b = find(o, hash(o, capacity)) >= 0;
        return b;
    }

    protected V put(K e, V value) {
        var result = capacity == 0 ? -1 : find(e, hash(e, capacity));
        if (result < 0) {
            if (size >= threshold) {
                grow();
                result = find(e, hash(e, capacity));
            }
            var index = -result - 1;
            shiftForInsertion(index);
            V oldValue = getValueFromTable(index);
            setKeyInTable(index, e);
            setValueTable(index, value);
            size++;
            modCount++;
            return oldValue;
        } else {
            return null;
        }
    }

    protected abstract void setValueTable(int index, V value);

    @SuppressWarnings("unchecked")
    protected  void resize(int newCapacity) {
        Object[] objects = toArray();
        computeThreshold(newCapacity);
        createTable(newCapacity);
        for (var i = 0; i < objects.length; i+=2) {
            var o=objects[i];
            int result = find(o, hash(o, newCapacity));
            var index = -result - 1;
            shiftForInsertion(index);
            setKeyInTable(index, (K)o);
            setValueInTable(index, (V)objects[i+1]);
        }
    }
    protected abstract void setValueInTable(int index, V e);

    protected abstract Object[] toArray();

    protected V get(Object o) {
        var h = hash(o, capacity);
        var index = find(o, h);
        if (index < 0) {
            return null;
        } else {
            return getValueFromTable(index);
        }
    }

    protected V remove(Object o) {
        var h = hash(o, capacity);
        var index = find(o, h);
        if (index < 0) {
            return null;
        } else {
            V oldValue=getValueFromTable(index);
            unsetTable(index);
            size--;
            modCount++;// must be done before shift, because debugger may advance iterator
            shiftForRemoval(index);
            return oldValue;
        }
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

    protected abstract V getValueFromTable(int index);
    protected abstract void unsetTable(int index);
    protected abstract void shiftForRemoval(int index);
    protected void clear() {
        clearTable();
        size = 0;
        modCount++;
    }

    protected abstract void clearTable();


    @Override
    protected AbstractRobinHoodHashMap clone() {
            @SuppressWarnings("unchecked")
            var that = (AbstractRobinHoodHashMap) super.clone();
        return that;
    }
}
