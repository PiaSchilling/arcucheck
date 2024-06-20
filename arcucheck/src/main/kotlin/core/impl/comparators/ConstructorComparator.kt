package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLClass
import core.model.puml.PUMLConstructor

/**
 * Compares the design of the constructors to the according implementation to detect any deviations
 *
 * @constructor just initializes fields, those are only necessary, so they can be added to the
 * output for more precise warnings
 *
 *
 * @param designDiagramPath the path to the diagram representing the design
 * @param implPath the path to the implementation which is compared to the design
 */
class ConstructorComparator(private val designDiagramPath: String, private val implPath: String) {
    /**
     * Compare all constructors contained in the provided classes to detect any deviations.
     * Only constructors for "correct" classes are compared. Correct means, that class signatures don't have any deviations.
     *
     * @param implementationClasses the classes as they exist in the implementation
     * @param designClasses the same classes as present in the design
     * @return a list of all detected deviations between the design and implementation classes constructors
     */
    fun comparePUMLConstructors(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>() // TODO duplicate code in method comparator

        val implClassesMap = implementationClasses.associateBy { it.fullName }
        val designClassesMap = designClasses.associateBy { it.fullName }

        val correctClassesNames = implClassesMap.keys.intersect(designClassesMap.keys)

        // Only check fields for correct classes (correct means: class signatures don't have any deviations)
        correctClassesNames.forEach { correctClassName ->
            val correctImplClass = implClassesMap[correctClassName]
            val correctDesignClass = designClassesMap[correctClassName]

            if (correctImplClass != null && correctDesignClass != null) {
                deviations.addAll(checkConstructors(correctImplClass, correctDesignClass))
            }

        }
        return deviations
    }

    /**
     * Check if any constructor of the provided classes are unexpected, absent of wrongly implemented
     *
     * @param implementationClass a class as it exists in the implementation
     * @param designClass the same class as is present in the design
     * @return a list of all detected deviations between the design and implementation classes constructors
     */
    private fun checkConstructors(
        implementationClass: PUMLClass,
        designClass: PUMLClass
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        val maybeAbsentConstructors = designClass.constructors.subtract(implementationClass.constructors.toSet())
        val maybeUnexpectedConstructors = implementationClass.constructors.subtract(designClass.constructors.toSet())

        if (maybeAbsentConstructors.isNotEmpty()) {
            val absentOrWrongFields = checkDeviatingConstructors(
                maybeAbsentConstructors.toList(),
                implementationClass,
                designClass,
                DeviationType.ABSENCE
            )
            deviations.addAll(absentOrWrongFields)
        }
        if (maybeUnexpectedConstructors.isNotEmpty()) {
            val unexpectedOrWrongFields = checkDeviatingConstructors(
                maybeUnexpectedConstructors.toList(),
                implementationClass,
                designClass,
                DeviationType.UNEXPECTED
            )
            deviations.addAll(unexpectedOrWrongFields)
        }

        return deviations
    }

    /**
     * Find the deviation cause of constructors: Deviation could be caused by absent constructor, unexpected constructor
     * or wrong implemented constructor
     *
     * @param deviatingConstructors a list of constructors which deviate from the design, that need to be checked
     * @param implementationClass the implemented class containing the deviating constructors
     * @param designClass the design of the class containing the deviating constructors
     * @param deviationType fields can either be suspected to be ABSENT or UNEXPECTED, deviationType controls the behavior of this function
     * @return a list containing all detected deviations
     */
    private fun checkDeviatingConstructors(
        deviatingConstructors: List<PUMLConstructor>,
        implementationClass: PUMLClass,
        designClass: PUMLClass,
        deviationType: DeviationType
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        val deviatingConstructorsMap = deviatingConstructors.associateBy { it.parameterTypes }
        val implementationConstructorsMap = implementationClass.constructors.associateBy { it.parameterTypes }
        val designConstructorMap = designClass.constructors.associateBy { it.parameterTypes }

        deviatingConstructorsMap.forEach { constructor ->
            val match = when (deviationType) {
                DeviationType.ABSENCE -> implementationConstructorsMap[constructor.key]
                DeviationType.UNEXPECTED -> designConstructorMap[constructor.key]
                else -> implementationConstructorsMap[constructor.key]
            }
            match?.let {
                // prevent bidirectional/duplicate adding of deviations -> only execute block below if match is a designClass
                // -> match is designClass if the type is UNEXPECTED
                if (deviationType == DeviationType.UNEXPECTED) {
                    deviations.add(
                        Deviation(
                            DeviationLevel.MIKRO,
                            DeviationArea.PROPERTY,
                            DeviationType.MISIMPLEMENTED,
                            listOf(designClass.name),
                            "Deviating constructor implementation",
                            "Implementation of constructor in class ${designClass.fullName}" +
                                    " deviates from the design: Constructor should have the visibility ${match.visibility} according to " +
                                    "the design but has the visibility ${constructor.value.visibility} in the implementation",
                            designDiagramPath,
                            implPath,
                        )
                    )
                }
            } ?: run {
                val deviationLocation =
                    "Constructor with the parameters ${constructor.value.parameterTypes} in the class \"${designClass.fullName}\""

                deviations.add( // If method still can not be found, then it will be marked as absent/unexpected
                    Deviation(
                        DeviationLevel.MIKRO,
                        DeviationArea.PROPERTY,
                        deviationType,
                        listOf(designClass.name),
                        "${deviationType.asAdjective} constructor",
                        when (deviationType) {
                            DeviationType.UNEXPECTED -> "$deviationLocation is not expected according to the design but present in the implementation."
                            DeviationType.ABSENCE -> "$deviationLocation is expected according to the design but not present in the implementation."
                            else -> ""
                        },
                        designDiagramPath,
                        implPath,
                    )
                )
            }
        }
        return deviations
    }

}