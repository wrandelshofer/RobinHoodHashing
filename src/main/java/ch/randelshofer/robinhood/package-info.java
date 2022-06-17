/**
 Provides Hash Sets and Hash Maps with Robin Hood Hashing.
 <p>
 Robin Hood Hashing is a hashing algorithm with open addressing hash table
 and linear probing.
 <p>
 Insertion and deletion is cost-based, which keeps the number of probes needed
 to find a desired element minimal.
 <p>
 For each element we compute a hash code {@code h} in the range
 {@code [0, table.length)}. {@code h} is the preferred bucket index of the
 element.
 <p>
 We then compute a sort-key {@code k} based on {@code h} and the current index
 {@code i} of the bucket in the table.
 {@code k = (h >= i) ? h : h - table.length}.
 The sort-key takes into account, that we have to wrap around at the end of
 the table when we are probing for an element.
 <p>
 The cost {@code c} for linear probing of an element with hash code {@code h}
 in bucket {@code i} in the table is:
 {@code c = (h <= i) ? i - h : i - h + table.length }.
 <p>
 Elements with a low distance from their preferred bucket are considered to be
 <i>'rich'</i>, elements with a high distance from their preferred bucket are
 considered to be <i>'poor'</i>.
 <p>
 Invariants:
 <ul>
 <li>The number of non-empty elements is in the range {@code [0, table.length)}.</li>
 <li>All non-empty elements in the table are sorted by their sort-key
 {@code k}.</li>
 <li>The total search lengths for all elements is minimal.</li>
 <li>The search lengths for all elements in the set has minimal variance.
 </li>
 </ul>
 p>
 Optimal load factor:
 p>
 In open addressing hash tables with linear probing, the entries tend to
 cluster together into contiguous groups. To avoid long probing sequences,
 we propose to never fill the table by more than 50 %.
 p>
 Robert Sedgewick, Kevin Wayne (2011) propose:
 "Proposition M. In a linear-probing hash table of size {@code m} and
 {@code n = α × m} keys, the average number of probes required is
 between ~ {@code 0.5 * (1 + 1/(1-α))} and ~ {@code 0.5 * (1 + 1/(1-α)²)}."
 p>
 <table width="100%">
 <tr><th>α</th><th>~min</th><th>~max</th></tr>
 <tr><td>0.1</td><td>0.555̅</td><td>0.62</td></tr>
 <tr><td>0.25</td><td>0.666̅</td><td>0.888̅</td></tr>
 <tr><td>0.5</td><td>1</td><td>2</td></tr>
 <tr><td>0.75</td><td>2</td><td>8</td></tr>
 <tr><td>0.9</td><td>5</td><td>50</td></tr>
 </table>
 <p>
 p>
 References:
 <dl>
 <dt>Robert Sedgewick, Kevin Wayne (2011). Algorithms, 4th Edition.</dt>
 <dd><a href="https://algs4.cs.princeton.edu/home/">princeton.edu</a>


 <dt>Austin Appleby (2008). MurmurHash</dt>
 <dd><a href="https://en.wikipedia.org/wiki/MurmurHash#Algorithm">wikipedia.org</a></dd>

 <dt>Daniel Lemire (2019). Fast Random Integer Generation in an Interval.
 ACM Transactions on Modeling and Computer Simulation.
 January 2019. Article No. 3.</dt>
 <dd><a href="https://arxiv.org/pdf/1805.10941.pdf">arxiv.org</a></dd>

 <dt>Daniel Lemire, Owen Kaser, Nathan Kurz (2018).
 Faster Remainder by Direct Computation.
 Applications to Compilers and Software Libraries.
 </dt>
 <dd><a href="https://arxiv.org/pdf/1902.01961.pdf">arxiv.org</a></dd>

 <dt>Emmanuel Goossaert (2013). Robin Hood hashing: backward shift deletion.</dt>
 <dd><a href="https://codecapsule.com/2013/11/17/robin-hood-hashing-backward-shift-deletion/">codecapsule.com</a></dd>

 <dt>Pedro Celis (1986). Robin Hood Hashing.
 Data Structuring Group. Department of Computer Science.
 University of Waterloo. Waterloo, Ontario, N2L 3G1.</dt>
 <dd><a href="https://cs.uwaterloo.ca/research/tr/1986/CS-86-14.pdf">cs.waterloo.ca</a></dd>

 <dt>Sebastiano Vigna (2002-2021). FastUtil. Apache License 2.0.</dt>
 <dd><a href="https://github.com/vigna/fastutil">github.com</a></dd>
 </dl>
 */
package ch.randelshofer.robinhood;