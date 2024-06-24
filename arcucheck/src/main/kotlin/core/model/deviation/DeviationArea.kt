package core.model.deviation

enum class DeviationArea {
    /**
     * Deviations in relations
     *
     */
    RELATION,

    /**
     * Deviations in packages
     *
     */
    PACKAGE,



    /**
     * Deviations in classes
     *
     */
    CLASS,

    /**
     * Deviations in interfaces
     *
     */
    INTERFACE,

    /**
     * Deviations in methods
     *
     */
    METHOD,

    /**
     * Deviations in constructors
     */
    CONSTRUCTOR,

    /**
     * Deviations in fields
     */
    FIELD,
}