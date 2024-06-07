package core.model.deviation

enum class DeviationType {
    /**
     * Missing in the implementation, but expected in the design
     */
    ABSENCE,

    /**
     * Present in the implementation, but not expected in the design
     */
    UNEXPECTED,

    /**
     * Present in the implementation, and expected in the design but wrongly implemented
     */
    MISIMPLEMENTED
}