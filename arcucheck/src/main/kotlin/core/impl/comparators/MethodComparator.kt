package core.impl.comparators

import core.impl.DeviationBuilder
import core.model.deviation.*
import core.model.puml.PUMLMethod
import core.model.puml.PUMLType

/**
 * Compares the design of Methods to the according implementation to detect any deviations
 *
 * @constructor just initializes fields, those are only necessary, so they can be added to the
 * output for more precise warnings
 *
 *
 * @param designDiagramPath the path to the diagram representing the design
 * @param implPath the path to the implementation which is compared to the design
 */
class MethodComparator(private val designDiagramPath: String, private val implPath: String) {

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
     * Find the deviation cause of methods: Deviation could be caused by absent method, unexpected method or wrong implemented method
     *
     * @param deviatingMethods a list of methods which deviate from the design, that need to be checked
     * @param implementationClass the implemented class containing the deviating methods
     * @param designClass the design of the class containing the deviating methods
     * @param deviationType methods can either be suspected to be ABSENT or UNEXPECTED, deviationType controls the  behavior of this function
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
        val implementationMethodsMap = implementationClass.methods.associateBy { it.name }
        val designMethodsMap = designClass.methods.associateBy { it.name }

        deviatingMethodsMap.forEach { method ->
            val match = when (deviationType) {
                DeviationType.ABSENCE -> implementationMethodsMap[method.key]
                DeviationType.UNEXPECTED -> designMethodsMap[method.key]
                else -> implementationMethodsMap[method.key]
            }
            match?.let {
                // prevent bidirectional/duplicate adding of deviations -> only execute block below if match is a designClass
                // -> match is designClass if the type is UNEXPECTED
                if (deviationType == DeviationType.UNEXPECTED) {
                    val deviationCauses = findDeviationCauses(method.value, match)
                    deviations.add(
                        DeviationBuilder.buildMisimplementedDeviation(
                            level = DeviationLevel.MIKRO,
                            subjectType = DeviationSubjectType.METHOD,
                            affectedClassName = designClass.name,
                            subjectName = method.value.name,
                            classLocation = designClass.pumlPackage.fullName,
                            causes = deviationCauses,
                            designDiagramPath = designDiagramPath,
                            implPath = implPath,
                        )
                    )
                }
            } ?: run {
                deviations.add( // If method still can not be found, then it will be marked as absent/unexpected
                    DeviationBuilder.buildUnexpectedAbsentDeviation(
                        level = DeviationLevel.MIKRO,
                        subjectType = DeviationSubjectType.METHOD,
                        deviationType = deviationType,
                        affectedClassName = designClass.name,
                        subjectName = method.value.name,
                        classLocation = designClass.pumlPackage.fullName,
                        designDiagramPath = designDiagramPath,
                        implPath = implPath,
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
    private fun findDeviationCauses(
        implementationMethod: PUMLMethod,
        designMethod: PUMLMethod
    ): List<String> {
        val methodWarnings = mutableListOf<String>()

        if (implementationMethod.isAbstract && !designMethod.isAbstract) {
            methodWarnings.add("Method \"${designMethod.name}\" is marked as abstract in the implementation but should not be abstract according to the design.")
        } else if (!implementationMethod.isAbstract && designMethod.isAbstract) {
            methodWarnings.add("Method \"${designMethod.name}\" should be abstract according to the design but is not marked as abstract in the implementation.")
        }



        if (implementationMethod.isStatic && !designMethod.isStatic) {
            methodWarnings.add("Method \"${designMethod.name}\" is marked as static in the implementation but should not be static according to the design.")
        } else if (!implementationMethod.isStatic && designMethod.isStatic) {
            methodWarnings.add("Method \"${designMethod.name}\" should be static according to the design but is not marked as static in the implementation.")
        }

        if (implementationMethod.visibility != designMethod.visibility) {
            methodWarnings.add(
                "Method \"${designMethod.name}\" should have the visibility \"${designMethod.visibility}\" according to the design but has the " +
                        "visibility \"${implementationMethod.visibility}\" in the implementation."
            )
        }

        if (implementationMethod.returnType != designMethod.returnType) {
            methodWarnings.add(
                "Method \"${designMethod.name}\" should have the return type \"${designMethod.returnType}\" according to the design but has the " +
                        "return type \"${implementationMethod.returnType}\" in the implementation."
            )
        }

        if (implementationMethod.parameterTypes != designMethod.parameterTypes) {
            methodWarnings.add(
                "Method \"${designMethod.name}\" should have the parameter types ${designMethod.parameterTypes} according to the design but has the " +
                        "parameter types ${implementationMethod.parameterTypes} in the implementation." // TODO shorten descriptions
            )
        }

        return methodWarnings
    }
}