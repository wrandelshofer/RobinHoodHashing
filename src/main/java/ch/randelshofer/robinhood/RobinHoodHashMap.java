package ch.randelshofer.robinhood;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static ch.randelshofer.robinhood.AvalancheAlgorithms.goldenRatioAvalanche;
import static ch.randelshofer.robinhood.RangeAlgorithms.fastRange;

public class RobinHoodHashMap<K, V> extends AbstractMutableRobinHoodHashMap<K, V> {
    private Object[] table;

    public RobinHoodHashMap() {
    }

    public RobinHoodHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public RobinHoodHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public RobinHoodHashMap(Map<? extends K, ? extends V> m, float loadFactor) {
        this(m, (int) (m.size() / loadFactor), loadFactor);
    }

    public RobinHoodHashMap(Collection<? extends Entry<? extends K, ? extends V>> entries, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        for (Entry<? extends K, ? extends V> entry : entries) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public RobinHoodHashMap(Collection<? extends Entry<? extends K, ? extends V>> entries) {
        this(entries, entries.size() * 2, 0.5f);
    }

    public RobinHoodHashMap(Map<? extends K, ? extends V> m, int initialCapacity, float loadFactor) {
        this(m.entrySet(), initialCapacity, loadFactor);
    }

    public RobinHoodHashMap(Map<? extends K, ? extends V> m) {
        this(m, m.size() * 2, 0.5f);
    }

    @Override
    protected void clearTable() {
        Arrays.fill(table, null);
    }

    @Override
    public RobinHoodHashMap<K, V> clone() {
        try {
            @SuppressWarnings("unchecked")
            RobinHoodHashMap<K, V> that = (RobinHoodHashMap<K, V>) super.clone();
            that.table = this.table.clone();
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    protected void createTable(int capacity) {
        this.table = new Object[capacity * 2];
    }

    @SuppressWarnings("unchecked")
    @Override
    protected K getKeyFromTable(int index) {
        return (K) table[index * 2];
    }

    @SuppressWarnings("unchecked")
    @Override
    protected V getValueFromTable(int index) {
        return (V) table[index * 2 + 1];
    }

    @Override
    protected int hash(Object e, int length) {
        return fastRange(goldenRatioAvalanche(Objects.hashCode(e)), length);
    }

    @Override
    protected boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }

    @Override
    protected void setKeyInTable(int index, K k) {
        table[index * 2] = k;
    }

    @Override
    protected void setValueInTable(int index, V value) {
        table[index * 2 + 1] = value;
    }

    @Override
    protected void setValueTable(int index, V k) {
        table[index * 2 + 1] = k;
    }

    @Override
    protected void shiftForInsertion(int index) {
        shiftForInsertion2(index, table);
    }

    @Override
    protected void shiftForRemoval(int index) {
        shiftForRemoval2(index, table);
    }

    protected Object[] toArray() {
        var r = new Object[size() * 2];
        int index = 0;
        for (var i = 0; i < table.length; i += 2) {
            if (table[i] != null) {
                r[index] = table[i];
                r[index + 1] = table[i + 1];
                index += 2;
            }
        }
        return r;
    }

    @Override
    protected void unsetTable(int index) {
        table[index * 2] = null;
        table[index * 2 + 1] = null;
    }
}
