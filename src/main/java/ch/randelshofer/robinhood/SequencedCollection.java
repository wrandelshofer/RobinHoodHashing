package ch.randelshofer.robinhood;

/**
 A collection with a well-defined linear ordering of its elements.
 <p>
 See <a href="https://openjdk.java.net/jeps/8280836>JEP 8280836</a>

 @param <E> the element type */
public interface SequencedCollection<E> {
    /**
     Gets the first element.

     @return an element
     @throws java.util.NoSuchElementException if the collection is empty
     */
    E getFirst();

    /**
     Gets the last element.

     @return an element
     @throws java.util.NoSuchElementException if the collection is empty
     */
    E getLast();

    /**
     Removes the first element.

     @return an element
     @throws java.util.NoSuchElementException if the collection is empty
     */
    E removeFirst();

    /**
     Removes the last element.

     @return an element
     @throws java.util.NoSuchElementException if the collection is empty
     */
    E removeLast();
}
