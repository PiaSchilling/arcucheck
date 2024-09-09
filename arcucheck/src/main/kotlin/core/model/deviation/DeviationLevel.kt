package core.model.deviation

/**
 * Enum representing different levels of deviations in software architecture.
 *
 * These levels categorize deviations based on their impact and scope:
 * - `MAKRO`: Deviations relevant at the architectural level, impacting overall structure and design.
 * - `MIKRO`: Deviations relevant at the code level, affecting specific implementations and details.
 */
enum class DeviationLevel {
    /**
     * Deviations which are relevant on architectural level
     */
    MAKRO,

    /**
     * Deviations which are relevant on code level
     */
    MIKRO
}