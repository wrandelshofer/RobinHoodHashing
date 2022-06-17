# Robin Hood hashing

Experimental implementation of the noble Robin Hood hashing algorithm which
takes from the rich and gives to the poor.

Robin Hood hashing is an 'open addressing hashing' algorithm with 'linear
probing'. The algorithm globally minimizes the number of probes needed to
find a desired element (probe count).
It also minimizes the variance of the probe counts.

The algorithm achieves this, by keeping all non-empty elements in the table
sorted.

## The algorithm

For each element we compute a hash value `h` in the range `[0, table.length)`.
`h` is the preferred bucket index of the element.

We then compute a sort-key `k` based on `h` and the current index `i` of the
bucket in the table. `k = (h >= i) ? h : h - table.length`.
The sort-key takes into account, that we have to wrap around at the end of
the table when we are probing for an element.

The cost `c` for linear probing of an element with hash code `h` in
bucket `i` in the table is: `c = (h <= i) ? i - h : i - h + table.length`.

Elements with a low distance from their preferred bucket are considered to be
_'rich'_, elements with a high distance from their preferred bucket are
considered to be _'poor'_.

### Inserting a new element

We start probing at index `i=h`. The sort-key is `k=h`.

If the bucket at `table[i]` is empty, we insert the new element `e`
there, and we are done.

If the bucket is occupied, we search along the probe sequence until we
find a bucket that is either empty or contains an element `ee` that has
a smaller sort key `k` for its bucket than `e` will have for that bucket.
We then shift `ee` and all following non-empty elements one bucket to
the right and insert `e`. This is an insertion sort algorithm.

Note, that if we reach the end of the table during our search along
the probe sequence, we wrap around with index `i=0`.
The sort-key of the element then becomes `k = h - table.length`.

### Deleting an element

When we remove an element, we shift back all elements that were displaced by
it.

We shift back all non-empty elements along the probe sequence until we find an
element with zero cost `c`, or an empty bucket.
Again, when searching along the probe sequence, we must wrap around at the end
of the table.

In particular, the algorithm does not create a tombstone, like some other open
addressing hashing algorithms do.

### Searching for an element

We start probing at index `i=h`. The sort-key is `k=h`.

If the bucket at `table[i]` is empty, we know that the element is not in the
table.

If the bucket is occupied, we search along the probe sequence until we find a
bucket that contains the element. We can abort probing if we find an empty
bucket or an element with a higher key than `k`.

### Invariants

1. The table always has at least one empty bucket.

2. All non-empty elements in the table are sorted by their sort-key `k`.

3. The total probe count for all elements is minimal.

4. The probe counts for all elements in the set has minimal variance.
