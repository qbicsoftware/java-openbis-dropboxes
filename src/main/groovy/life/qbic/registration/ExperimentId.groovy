package life.qbic.registration

/**
 * <p>QExperimentType identifier object.</p>
 *
 * <p>Full experiment run identifiers have the common form like:</p>
 *
 * <ul>
 *     <li>QTESTE1</li>
 *     <li>Q0011E12</li>
 *     <li>...</li>
 * </ul>
 *
 * <p>Containing the project code in the beginning and then the running identifier which is an
 * Integer prefixed by an capital 'E', like 'E1' or 'E12' in the examples above.</p>
 * <br>
 * <p>This class helps to interact with exactly this latter part, and to read out the running
 * Integer value.</p>
 *
 * @since 1.0.0
 */
class ExperimentId implements Increment<ExperimentId>, Comparable<ExperimentId>{

    final Integer experimentId

    private static final String PREFIX = 'E'

    /**
     * Creates an instance of an {@link ExperimentId} based on a String representation of a
     * full analysis result id.
     *
     * @param experimentId the String representation of a full analysis result id.
     * @return An instance of type {@link ExperimentId}
     * @throws NumberFormatException if the suffix part cannot be parsed.
     * @since 1.0.0
     */
    static ExperimentId parseFrom(String experimentId) throws NumberFormatException {
        def matcher = experimentId =~ /E+(\d*)$/
        if (matcher) {
            def currentId = Integer.parseInt((String) matcher[0][1])
            new ExperimentId(currentId)
        } else {
            throw new NumberFormatException("Could not process result id tag.")
        }
    }

    /**
     * Creates an instance of an {@link ExperimentId} based on a given Integer value.
     * @param value the running Integer value of the identifier.
     * @since 1.0.0
     */
    ExperimentId(Integer value) {
        this.experimentId = value
    }

    /**
     * <p>Return the next experiment increment.</p>
     * @return An increment of the current {@link ExperimentId}
     * @since 1.0.0
     */
    @Override
    ExperimentId nextId() {
        return new ExperimentId(this.experimentId+1)
    }

    /**
     * Returns the running number of an analysis run identifier.
     * @return The running number of the identifier.
     * @since 1.0.0
     */
    Integer getId() {
        return this.experimentId
    }

    /**
     * <p>Returns the String representation of an experiment identifier containing the
     * prefix `E`.</p>
     * <br>
     * <strong>Example:</strong>
     * <p>If the current value of the identifier is 2, then the <code>toString()</code> method
     * returns 'E2'.</p>
     * @return The string representation of the identifier suffix.
     * @since 1.0.0
     */
    @Override
    String toString() {
        return "${PREFIX}${experimentId}"
    }

    @Override
    int compareTo(ExperimentId o) {
        Objects.requireNonNull(o)
        return compareExperimentId(o)
    }

    private int compareExperimentId(ExperimentId o) {
        int difference = this.experimentId - o.getExperimentId()
        int result = 0
        switch (difference) {
            case { it > 0 }:
                result = 1
                break
            case { it < 0 }:
                result = -1
                break
        }
        return result
    }
}
