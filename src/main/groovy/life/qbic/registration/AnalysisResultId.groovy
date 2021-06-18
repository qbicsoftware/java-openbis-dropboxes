package life.qbic.registration

import groovy.transform.EqualsAndHashCode

import javax.validation.constraints.NotNull

/**
 * <p>Analysis run identifier.</p>
 *
 * <p>Full analysis run identifiers have the common form like:</p>
 *
 * <ul>
 *     <li>QTESTE10R1</li>
 *     <li>Q0011E12R12</li>
 *     <li>...</li>
 * </ul>
 *
 * <p>Containing the project code in the beginning and then the running identifier which is an
 * Integer prefixed by an capital 'R', like 'R1' or 'R12' in the examples above.</p>
 * <br>
 * <p>This class helps to interact with exactly this latter part, and to read out the running
 * Integer value.</p>
 *
 * @since 1.0.0
 */
@EqualsAndHashCode(includeFields = true)
class AnalysisResultId implements Increment<AnalysisResultId>, Comparable<AnalysisResultId> {

    private final Integer resultId

    private static final String PREFIX = 'R'

    /**
     * Creates an instance of an {@link AnalysisResultId} based on a String representation of a
     * full analysis result id.
     *
     * @param analysisResultId the String representation of a full analysis result id.
     * @return An instance of type {@link AnalysisResultId}
     * @throws NumberFormatException if the suffix part cannot be parsed.
     * @since 1.0.0
     */
    static AnalysisResultId parseFrom(String analysisResultId) throws NumberFormatException {
        def matcher = analysisResultId =~ /R+(\d*)$/
        if (matcher) {
            def currentId = Integer.parseInt((String) matcher[0][1])
            new AnalysisResultId(currentId)
        } else {
            throw new NumberFormatException("Could not process result id tag.")
        }
    }

    /**
     * Creates an instance of an {@link AnalysisResultId} based on a given Integer value.
     * @param value the running Integer value of the identifier.
     * @since 1.0.0
     */
    AnalysisResultId(Integer value) {
        this.resultId = value
    }


    /**
     * Returns the running number of an analysis run identifier.
     * @return The running number of the identifier.
     * @since 1.0.0
     */
    Integer getId() {
        return this.resultId
    }

    /**
     * Creates a new {@link AnalysisResultId} instance with an incremented running number of +1.
     * @return An incremented {@link AnalysisResultId} object.
     * @since 1.0.0
     */
    AnalysisResultId nextId() {
        new AnalysisResultId(this.id + 1)
    }

    /**
     * <p>Returns the String representation of an analysis run identifier containing the
     * prefix `R`.</p>
     * <br>
     * <strong>Example:</strong>
     * <p>If the current value of the identifier is 2, then the <code>toString()</code> method
     * returns 'R2'.</p>
     * @return The string representation of the identifier suffix.
     * @since 1.0.0
     */
    @Override
    String toString() {
        return "${PREFIX}${id}"
    }

    @Override
    int compareTo(@NotNull AnalysisResultId o) {
        Objects.requireNonNull(o)
        return compareAnalysisId(o)
    }

    private int compareAnalysisId(AnalysisResultId otherId) {
        int difference = this.id - otherId.id
        int result = 0
        println difference
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
