package ch.randelshofer.robinhood;


import java.util.Arrays;
import java.util.Collection;

import static ch.randelshofer.robinhood.AvalancheAlgorithms.goldenRatioAvalanche;
import static ch.randelshofer.robinhood.RangeAlgorithms.fastRange;
import static java.lang.Math.max;

/**
 * Identity Robin Hood Hash Set.
 * <ul>
 *     <li>Elements are distinguished by their object identity and their
 *     hash code is generated with {@link System#identityHashCode(Object)}.</li>
 *     <li>Iteration order is not guaranteed.</li>
 * </ul>
 */
public class IdentityRobinHoodHashSet<E> extends AbstractMutableRobinHoodHashSet<E> {
    private Object[] table;

    public IdentityRobinHoodHashSet() {
        super();
    }

    public IdentityRobinHoodHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentityRobinHoodHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IdentityRobinHoodHashSet(Collection<? extends E> c) {
        super(c);
    }

    public IdentityRobinHoodHashSet(Collection<? extends E> c, int initialCapacity, float loadFactor) {
        super(c, initialCapacity,loadFactor);
    }

    @Override
    protected void clearTable() {
        Arrays.fill(table, null);
    }

    @Override
    public IdentityRobinHoodHashSet<E> clone() {
        IdentityRobinHoodHashSet<E> that = (IdentityRobinHoodHashSet<E>) super.clone();
        that.table = this.table.clone();
        return that;
    }

    @Override
    protected void createTable(int capacity) {
        table = new Object[capacity];
    }

    @SuppressWarnings("unchecked")
    @Override
    protected E getKeyFromTable(int index) {
        return (E)table[index];
    }

    @Override
    protected int hash(Object e, int length) {
        return fastRange(goldenRatioAvalanche(System.identityHashCode(e)), length);
    }

    protected boolean isEqual(Object a, Object b) {
        return a == b;
    }

    @Override
    protected void setKeyInTable(int index, E e) {
        table[index]=e;
    }

    @Override
    protected void shiftForInsertion(int index) {
        shiftForInsertion1(index,table);
    }

    @Override
    protected void shiftForRemoval(int index) {
        shiftForRemoval1(index,table);
    }

    @Override
    protected void unsetTable(int index) {
        table[index]=null;
    }
}
