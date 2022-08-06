package ch.randelshofer.robinhood;


import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.max;

/**
 Extends {@link AbstractRobinHoodHashSet} with the API from the {@link Set}
 interface.
 */
public abstract class AbstractMutableRobinHoodHashSet<E> extends AbstractRobinHoodHashSet<E>
        implements Set<E> {
    public AbstractMutableRobinHoodHashSet() {
        super();
    }

    public AbstractMutableRobinHoodHashSet(int expectedSize) {
        super(expectedSize);
    }

    public AbstractMutableRobinHoodHashSet(int expectedSize, float loadFactor) {
        super(expectedSize, loadFactor);
    }

    public AbstractMutableRobinHoodHashSet(Collection<? extends E> c) {
        this(c, c.size(), 0.5f);
    }

    public AbstractMutableRobinHoodHashSet(Collection<? extends E> c, int expectedSize, float loadFactor) {
        super(expectedSize, loadFactor);
        for (E e : c) {
            add(e);
        }
    }

    @Override
    public boolean add(E e) {
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o);
    }

    public boolean addAll(Collection<? extends E> c) {
        var modified = false;
        for (E e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        var modified = false;
        for (var it = iterator(); it.hasNext(); ) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        var modified = false;
        if (size() > c.size()) {
            for (Object e : c) {
                modified |= remove(e);
            }
        } else {
            for (var it = iterator(); it.hasNext(); ) {
                if (c.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        super.clear();
    }

    public void sizeToFit(float fillRatio) {
        if (size == 0) {
            capacity = 0;
            resize(0);
        } else {
            int newCapacity = roundCapacity(max((int) (size / fillRatio), size));
            if (newCapacity != capacity) {
                resize(newCapacity);
            }
        }
    }
}
