package ch.randelshofer.robinhood;


import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Extends {@link AbstractRobinHoodHashSet} with the API from the {@link Set}
 * interface.
 */
public abstract class AbstractMutableRobinHoodHashSet<E> extends AbstractRobinHoodHashSet<E>
        implements Set<E>, Serializable {
    public AbstractMutableRobinHoodHashSet() {
        super();
    }

    public AbstractMutableRobinHoodHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractMutableRobinHoodHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public AbstractMutableRobinHoodHashSet(Collection<? extends E> c) {
        this(c,c.size()*4,0.5f);
    }

    public AbstractMutableRobinHoodHashSet(Collection<? extends E> c, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        for (E e:c){
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
        for (var it = iterator(); it.hasNext();) {
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
        if (size()>c.size()){
            for (Object e : c) {
                modified |= remove(e);
            }
        }else {
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
}
