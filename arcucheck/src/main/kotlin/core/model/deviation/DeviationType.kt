package core.model.deviation

enum class DeviationType(val asAdjective:String) {
    /**
     * Missing in the implementation, but expected in the design
     */
    ABSENCE("Absent"),

    /**
     * Present in the implementation, but not expected in the design
     */
    UNEXPECTED("Unexpected"),

    /**
     * Present in the implementation, and expected in the design but wrongly implemented
     */
    MISIMPLEMENTED("Misimplemented")
}