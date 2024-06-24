package core.model.deviation

/**
 * Enum representing different types of subjects considered in deviation analysis. Each type corresponds to a specific
 * aspect of software architecture.
 *
 * Available subject types are: `RELATION, PACKAGE, CLASS, INTERFACE, CONSTRUCTOR, METHOD, FIELD`
 *
 * @property asString  first letter uppercase string representation of the subject type.
 */
enum class DeviationSubjectType(val asString: String) {
    /**
     * For deviations in relations
     *
     */
    RELATION("Relation"),

    /**
     * For deviations in packages
     *
     */
    PACKAGE("Package"),

    /**
     * For deviations in classes
     *
     */
    CLASS("Class"),

    /**
     * For deviations in interfaces
     *
     */
    INTERFACE("Interface"),

    /**
     * For deviations in methods
     *
     */
    METHOD("Method"),

    /**
     * For deviations in constructors
     */
    CONSTRUCTOR("Constructor"),

    /**
     * For deviations in fields
     */
    FIELD("Field"),
}