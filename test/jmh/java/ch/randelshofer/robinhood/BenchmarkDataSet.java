package ch.randelshofer.robinhood;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Random;
import java.util.Set;

public class BenchmarkDataSet {
    public static final class Key {
        private final int id;
        private final int hash;

        public Key(int id, int hashMask) {
            this.id = id;
            this.hash = id & hashMask;
        }

        public int id() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Key) obj;
            return this.id == that.id;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "Key[" +
                    "id=" + id + ']';
        }

    }

    public final int keyRangeMax;
    public final int keyRangeMin;
    public final int size;
    public final Key[] valuesInSet;
    public final Key[] valuesNotInSet;

    public final Set<Key> constantIdentitySet;
    public final IdentityHashMap<Key, Boolean> constantIdentityMap;
    public final IdentityHashMap<Key, Boolean> constantIdentityMapWithNewValues;

    public BenchmarkDataSet(int size, int keyRangeMin, int keyRangeMax, int hashMask) {
        this.keyRangeMax = keyRangeMax;
        this.keyRangeMin = keyRangeMin;
        this.size = size;
        valuesInSet = new Key[(int) (this.size)];
        valuesNotInSet = new Key[(int) (this.size)];
        ;
        Random rng = new Random(0);
        // to get the desired capacity, we have to initialize the map with
        // half of the capacity.
        Set<Key> set0 = new HashSet<>(this.size * 2);
        IdentityHashMap<Key, Boolean> identityMap = new IdentityHashMap<>(this.size);
        IdentityHashMap<Key, Boolean> identityMapWithNewValues = new IdentityHashMap<>(this.size);
        Set<Key> set = Collections.newSetFromMap(identityMap);
        for (int i = 0; i < valuesInSet.length; i++) {
            Key k;
            do {
                k = new Key(nextRng(rng), hashMask);
            } while (!set0.add(k));
            set.add(k);
            valuesInSet[i] = k;
        }
        for (Key k : set) {
            identityMapWithNewValues.put(k, false);
        }
        for (int i = 0; i < valuesInSet.length; i++) {
            Key k;
            do {
                k = new Key(nextRng(rng), hashMask);
            } while (set0.contains(k));
            valuesNotInSet[i] = k;
        }
        // shuffle arrays, because objects are allocated sequentially in memory
        Collections.shuffle(Arrays.asList(valuesInSet, new Random(0)));
        Collections.shuffle(Arrays.asList(valuesNotInSet, new Random(0)));

        constantIdentitySet = set;
        constantIdentityMap = identityMap;
        constantIdentityMapWithNewValues = identityMapWithNewValues;
    }

    private int nextRng(Random rng) {
        if (this.keyRangeMin == Integer.MIN_VALUE && this.keyRangeMax == Integer.MAX_VALUE) {
            return rng.nextInt();
        }
        return rng.nextInt(this.keyRangeMax - this.keyRangeMin + 1) + this.keyRangeMin;
    }
}
