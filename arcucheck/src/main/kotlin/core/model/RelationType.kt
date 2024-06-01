package core.model

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
    AGGREGATION("o--"),
}