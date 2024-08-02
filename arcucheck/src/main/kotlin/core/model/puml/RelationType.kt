package core.model.puml

enum class RelationType(val pumlIdentifier: String) {
    /**
     * Class extending another class (inheritance)
     *
     */
    INHERITANCE("<|--"),

    /**
     * Class implementing resp. realising an interface
     *
     */
    REALISATION("<|.."),

    /**
     * Class containing another class (composition). Contained class can not exist independently
     *
     */
    COMPOSITION("*--"),

    /**
     * Class containing another class (composition). Contained class can exist independently
     *
     */
    AGGREGATION("o--"),

    /**
     * Simple association between two classes
     *
     */
    ASSOCIATION("--"),

    /**
     * Association that it navigable from the class of the left side of the identifier
     *
     */
    LEFT_NAVIGABLE_ASSOCIATION("<--x"),

    /**
     * Association that it navigable from the class of the right side of the identifier
     *
     */
    RIGHT_NAVIGABLE_ASSOCIATION("x-->"),

    /**
     * Association that it navigable in both directions
     *
     */
    BIDIRECTIONAL_NAVIGABLE_ASSOCIATION("<-->");

    companion object {
        /**
         * Returns the matching RelationType enum constant for the provided string identifier
         *
         * @param string string representation of a relation (<|--,<|..,*..,o--)
         * @return the matching RelationType enum constant
         */
        fun fromString(string: String): RelationType {
            return RelationType.values().first { relationType -> relationType.pumlIdentifier == string }
        }
    }
}