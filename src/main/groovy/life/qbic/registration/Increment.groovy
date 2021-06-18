package life.qbic.registration

/**
 * <p>Increments are objects that have successors that have an incremented identifier.</p>
 *
 * <p>For example an increment of type {@link Integer}:</p>
 *
 * <pre>
 * <code>
 *
 * class MyInteger implements Increment&#60;MyInteger&#62; {
 *   private final Integer value = 1;
 *
 *   MyInteger(Integer value) { this.value = value }
 *
 *   MyInteger nextId() {
 *     return new MyInteger(this.value + 1)
 *   }
 * }
 *  </code>
 * </pre>
 *
 * @since 1.0.0
 */
interface Increment<T> {
    /**
     * <p>Creates a new instance of type <code>T</code>, which is the next logical increment of this object.</p>
     * <br>
     * <p>The following condition <strong>must</strong> be fulfilled:</p>
     * <br>
     * <p>
     *     given: b&#60;T> = a&#60;T>.nextId()
     *     <br>
     *     then: b&#60;T> != a&#60;T>
     * </p>
     * @return
     */
    T nextId()
}
