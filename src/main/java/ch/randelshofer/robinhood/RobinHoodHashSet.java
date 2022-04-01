package ch.randelshofer.robinhood;


import java.util.Arrays;
import java.util.Collection;

import static ch.randelshofer.robinhood.AvalancheAlgorithms.goldenRatioAvalanche;
import static ch.randelshofer.robinhood.RangeAlgorithms.fastRange;

/**
 * Robin Hood Hash Set.
 * <ul>
 *     <li>Elements are distinguished by their {@link Object#equals} method,
 *     and are hashed using their {@link Object#hashCode} method.</li>
 *     <li>Iteration order is not guaranteed.</li>
 * </ul>
 */
public class RobinHoodHashSet<E> extends AbstractMutableRobinHoodHashSet<E>
        implements Cloneable {

    private Object[] table;


    public RobinHoodHashSet() {
    }

    public RobinHoodHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public RobinHoodHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public RobinHoodHashSet(Collection<? extends E> c) {
        super(c);
    }

    public RobinHoodHashSet(Collection<? extends E> c, int initialCapacity, float loadFactor) {
        super(c, initialCapacity, loadFactor);
    }

    @Override
    protected void clearTable() {
        Arrays.fill(table, null);
    }

    @Override
    public RobinHoodHashSet<E> clone() {
        try {
            RobinHoodHashSet<E> that = (RobinHoodHashSet<E>) super.clone();
            that.table = this.table.clone();
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    protected void createTable(int capacity) {
        table = new Object[capacity];
    }

    @SuppressWarnings("unchecked")
    @Override
    protected E getKeyFromTable(int index) {
        return (E) table[index];
    }

    @Override
    protected int hash(Object e, int length) {
        return fastRange(goldenRatioAvalanche(e.hashCode()), length);
    }

    protected boolean isEqual(Object a, Object b) {
        return a.equals(b);
    }

    @Override
    protected void setKeyInTable(int index, E e) {
        table[index] = e;
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
        table[index] = null;
    }
}
