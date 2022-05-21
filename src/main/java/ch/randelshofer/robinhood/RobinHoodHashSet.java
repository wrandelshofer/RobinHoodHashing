package ch.randelshofer.robinhood;


import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;

import static ch.randelshofer.robinhood.AvalancheAlgorithms.goldenRatioAvalanche;
import static ch.randelshofer.robinhood.RangeAlgorithms.fastRange;

/**
 * Robin Hood Hash Set.
 * <ul>
 *     <li>Elements are distinguished by their {@link Object#equals} method,
 *     and are hashed using their {@link Object#hashCode} method.</li>
 *     <li>Iteration order is not guaranteed.</li>
 *     <li>Null values are not allowed.</li>
 * </ul>
 */
public class RobinHoodHashSet<E> extends AbstractMutableRobinHoodHashSet<E>
        implements Cloneable {

    private Object[] table;


    public RobinHoodHashSet() {
    }

    public RobinHoodHashSet(int expectedSize) {
        super(expectedSize);
    }

    public RobinHoodHashSet(int expectedSize, float loadFactor) {
        super(expectedSize, loadFactor);
    }

    public RobinHoodHashSet(Collection<? extends E> c) {
        super(c);
    }

    public RobinHoodHashSet(Collection<? extends E> c, int expectedSize, float loadFactor) {
        super(c, expectedSize, loadFactor);
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
        return fastRange(goldenRatioAvalanche(Objects.hashCode(e)), length);
    }

    protected boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
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

    public Spliterator<E> spliterator() {
        return new SetSpliterator(0, capacity);
    }
}
