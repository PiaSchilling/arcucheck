package core.model.PUML

enum class RelationType(val pumlIdentifier: String) {
    /**
     * Class extending another class (inheritance)
     *
     */
    EXTENSION("<|--"),

    /**
     * Class implementing an interface
     *
     */
    IMPLEMENTATION("<|.."),

    /**
     * Class containing another class (composition). Contained class can not exist independently
     *
     */
    COMPOSITION("*--"),

    /**
     * Class containing another class (composition). Contained class can exist independently
     *
     */
    AGGREGATION("o--");

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