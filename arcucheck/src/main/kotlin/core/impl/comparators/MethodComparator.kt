package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLMethod
import core.model.puml.PUMLType

class MethodComparator {

    /**
     * Compare all methods contained in the provided classes to detect any deviations.
     * Only methods for "correct" classes are compared. Correct means, that class signatures don't have any deviations.
     *
     * @param implementationClasses the classes as they exist in the implementation
     * @param designClasses the same classes as present in the design
     * @return a list of all detected deviations between the design and implementation classes methods
     */
    fun comparePUMLMethdos(
        implementationClasses: List<PUMLType>,
        designClasses: List<PUMLType>,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implClassesMap = implementationClasses.associateBy { it.fullName }
        val designClassesMap = designClasses.associateBy { it.fullName }

        val correctClassesNames = implClassesMap.keys.intersect(designClassesMap.keys)

        // Only check methods for correct classes (correct means: class signatures don't have any deviations)
        correctClassesNames.forEach { correctClassName ->
            val correctImplClass = implClassesMap[correctClassName]
            val correctDesignClass = designClassesMap[correctClassName]

            if (correctImplClass != null && correctDesignClass != null) {
                deviations.addAll(checkMethods(correctImplClass, correctDesignClass))
            }

        }

        return deviations
    }

    /**
     * Check if any methods of the provided classes are unexpected, absent of wrongly implemented
     *
     * @param implementationClass a class as it exists in the implementation
     * @param designClass the same class as is present in the design
     * @return a list of all detected deviations between the design and implementation classes methods
     */
    private fun checkMethods(
        implementationClass: PUMLType,
        designClass: PUMLType,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        val maybeAbsentMethods = designClass.methods.subtract(implementationClass.methods.toSet())
        val maybeUnexpectedMethods = implementationClass.methods.subtract(designClass.methods.toSet())

        if (maybeAbsentMethods.isNotEmpty()) {
            val absentDeviations = checkDeviatingMethods(
                maybeAbsentMethods.toList(),
                implementationClass,
                designClass,
                DeviationType.ABSENCE
            )
            deviations.addAll(absentDeviations)
        }
        if (maybeUnexpectedMethods.isNotEmpty()) {
            val unexpectedDeviations = checkDeviatingMethods(
                maybeUnexpectedMethods.toList(),
                implementationClass,
                designClass,
                DeviationType.UNEXPECTED
            )
            deviations.addAll(unexpectedDeviations)
        }
        return deviations.distinct()
    }

    /**
     * Check why methods deviate: Deviation could be caused by absent method, unexpected method or wrong implemented method
     *
     * @param deviatingMethods a list of methods which deviate from the design, that need to be checked
     * @param implementationClass the implemented class containing the deviating methods
     * @param designClass the design of the class containing the deviating methods
     * @param deviationType methods can either be suspected to be ABSENT or UNEXPECTED, parameter controls behavior of this function
     * @return a list containing all detected deviations
     */
    private fun checkDeviatingMethods(
        deviatingMethods: List<PUMLMethod>,
        implementationClass: PUMLType,
        designClass: PUMLType,
        deviationType: DeviationType
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val deviatingMethodsMap = deviatingMethods.associateBy { it.name }
        val implementationClassMap = implementationClass.methods.associateBy { it.name }
        val designClassMap = designClass.methods.associateBy { it.name }

        deviatingMethodsMap.forEach { method ->
            val match = when (deviationType) {
                DeviationType.ABSENCE -> implementationClassMap[method.key]
                DeviationType.UNEXPECTED -> designClassMap[method.key]
                else -> implementationClassMap[method.key]
            }
            match?.let {
                // prevent bidirectional/duplicate adding of deviations -> only execute block below if match is a designClass
                // -> match is designClass if the type is UNEXPECTED
                if (deviationType == DeviationType.UNEXPECTED) {
                    val deviationCauses = checkDeviationArea(method.value, match)
                    deviations.add(
                        Deviation(
                            DeviationLevel.MIKRO,
                            DeviationArea.BEHAVIOR, // TODO behavior is bad wording
                            DeviationType.MISIMPLEMENTED,
                            listOf(designClass.name),
                            "Maybe wrong implemented method", // TODO clean up message
                            "Method ${method.value.name} in class ${designClass.name} is implemented incorrectly: $deviationCauses"
                        )
                    )
                }
            } ?: run {
                deviations.add( // If method still can not be found, then it will be marked as absent/unexpected
                    Deviation(
                        DeviationLevel.MIKRO,
                        DeviationArea.BEHAVIOR, // TODO behavior is bad wording
                        deviationType,
                        listOf(designClass.name),
                        "$deviationType method", // TODO fix messages!!!
                        "Method ${method.value.name} in class ${designClass.name} is $deviationType"
                    )
                )
            }
        }
        return deviations
    }

    /**
     * Check in which area a method deviates from the design (wrong visibility, wrong return type, wrong parameters,
     * missing abstract or static modifier)
     * Could also be multiple areas (e.g. wrong visibility and missing abstract modifier)
     *
     * @param implementationMethod the implemented method (is-state)
     * @param designMethod intended design of the method (should-state)
     * @return a list containing all detected deviations
     */
    private fun checkDeviationArea(
        implementationMethod: PUMLMethod,
        designMethod: PUMLMethod
    ): List<String> {
        val methodWarnings = mutableListOf<String>()
        if (implementationMethod.isAbstract && !designMethod.isAbstract) {
            methodWarnings.add("Method ${designMethod.name} is marked as abstract but should not be abstract according to the design") // TODO revise text
        } else if (!implementationMethod.isAbstract && designMethod.isAbstract) {
            methodWarnings.add("Method ${designMethod.name} should be abstract according to the design but it not in the impl.")
        }

        if (implementationMethod.isStatic && !designMethod.isStatic) {
            methodWarnings.add("Method ${designMethod.name} is marked as static but should not be static according to the design") // TODO revise text
        } else if (!implementationMethod.isStatic && designMethod.isStatic) {
            methodWarnings.add("Method ${designMethod.name} should be static according to the design but it not in the impl.")
        }

        if (implementationMethod.visibility != designMethod.visibility) {
            methodWarnings.add(
                "Method ${designMethod.name} should have the Visbility ${designMethod.visibility} but has the " +
                        "visibility ${implementationMethod.visibility}"
            )
        }

        if (implementationMethod.returnType != designMethod.returnType) {
            methodWarnings.add(
                "Method ${designMethod.name} should have the return type ${designMethod.returnType} but has the " +
                        "return type ${implementationMethod.returnType}"
            )
        }

        if (implementationMethod.parameterTypes != designMethod.parameterTypes) {
            methodWarnings.add(
                "Method ${designMethod.name} should have the parameter types  ${designMethod.parameterTypes} but has the " +
                        "parameter types ${implementationMethod.parameterTypes}"
            )
        }

        return methodWarnings
    }
}