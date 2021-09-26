package ch.randelshofer.robinhood;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Random;
import java.util.Set;

public class BenchmarkDataSet {
    public static record Key(int id) {
    }

    public final int keyRangeMax;
    public final int keyRangeMin;
    public final int size;
    public final Key[] valuesInSet;
    public final Key[] valuesNotInSet;

    public final Set<Key> constantIdentitySet;
    public final IdentityHashMap<Key, Boolean> constantIdentityMap;

    public BenchmarkDataSet(int size, int keyRangeMin, int keyRangeMax) {
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
        Set<Key> set = Collections.newSetFromMap(identityMap);
        for (int i = 0; i < valuesInSet.length; i++) {
            Key k;
            do {
                k = new Key(nextRng(rng));
            } while (!set0.add(k));
            set.add(k);
            valuesInSet[i] = k;
        }

        for (int i = 0; i < valuesInSet.length; i++) {
            Key k;
            do {
                k = new Key(nextRng(rng));
            } while (set0.contains(k));
            valuesNotInSet[i] = k;
        }
        // shuffle arrays, because objects are allocated sequentially in memory
        Collections.shuffle(Arrays.asList(valuesInSet, new Random(0)));
        Collections.shuffle(Arrays.asList(valuesNotInSet, new Random(0)));

        constantIdentitySet = set;
        constantIdentityMap = identityMap;
    }

    private int nextRng(Random rng) {
        if (this.keyRangeMin == Integer.MIN_VALUE && this.keyRangeMax == Integer.MAX_VALUE) {
            return rng.nextInt();
        }
        return rng.nextInt(this.keyRangeMax - this.keyRangeMin + 1) + this.keyRangeMin;
    }
}
