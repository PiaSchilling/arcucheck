package core.model.deviation

/**
 * Enum representing different types of deviations.
 *
 * Available deviation types are: `ABSENCE, UNEXPECTED, MISIMPLEMENTED`
 *
 * @property asString first letter uppercase string representation of the deviation type.
 */
enum class DeviationType(val asString:String) {
    /**
     * Missing in the implementation, but expected in the design
     */
    ABSENT("Absent"),

    /**
     * Present in the implementation, but not expected in the design
     */
    UNEXPECTED("Unexpected"),

    /**
     * Present in the implementation, and expected in the design but wrongly implemented
     */
    MISIMPLEMENTED("Misimplemented")
}