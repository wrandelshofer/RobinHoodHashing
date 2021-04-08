/**
 * Provides Hash Sets and Hash Maps with Robin Hood Hashing.
 * <p>
 * Robin Hood Hashing is a hashing algorithm with an open addressing hash table.
 * Insertion and deletion is cost-based, which keeps the number of probes needed
 * to find a desired element minimal.
 * <p>
 * For each element we compute a hash value {@code h} in the range
 * {@code [0, table.length)}. {@code h} is the preferred bucket index of the
 * element.
 * <p>
 * We then compute a sort-key {@code k} based on {@code h} and the current index
 * {@code i} of the bucket in the table.
 * {@code k = (h >= i) ? h : h - table.length}.
 * The sort-key takes into account, that we have to wrap around at the end of
 * the table when we are probing for an element.
 * <p>
 * The cost {@code c} for linear probing of an element with hash code {@code h}
 * in bucket {@code i} in the table is:
 * {@code c = (h <= i) ? i - h : i - h + table.length }.
 * <p>
 * Elements with a low distance from their preferred bucket are considered to be
 * <i>'rich'</i>, elements with a high distance from their preferred bucket are
 * considered to be <i>'poor'</i>.
 * <p>
 * Invariants:
 * <ul>
 *     <li>The table always has at least one empty bucket.</li>
 *     <li>All non-empty elements in the table are sorted by their sort-key
 *     {@code k}.</li>
 *     <li>The total search lengths for all elements is minimal.</li>
 *     <li>The search lengths for all elements in the set has minimal variance.
 *     </li>
 * </ul>
 * <p>
 * References:
 * <dl>
 *     <dt>Pedro Celis (1986). Robin Hood Hashing.
 *     Data Structuring Group. Department of Computer Science.
 *     University of Waterloo. Waterloo, Ontario, N2L 3G1.</dt>
 *     <dd><a href="https://cs.uwaterloo.ca/research/tr/1986/CS-86-14.pdf">cs.waterloo.ca</a></dd>
 *
 *     <dt>Emmanuel Goossaert (2013). Robin Hood hashing: backward shift deletion.</dt>
 *     <dd><a href="https://codecapsule.com/2013/11/17/robin-hood-hashing-backward-shift-deletion/">codecapsule.com</a></dd>
 *
 *     <dt>Daniel Lemire (2019). Fast Random Integer Generation in an Interval.
 *     ACM Transactions on Modeling and Computer Simulation.
 *     January 2019. Article No. 3.</dt>
 *     <dd><a href="https://arxiv.org/pdf/1805.10941.pdf">arxiv.org</a></dd>
 *
 *     <dt>Austin Appleby (2008). MurmurHash</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/MurmurHash#Algorithm></a></a></dd>
 * </dl>
 */
package ch.randelshofer.robinhood;