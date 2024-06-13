package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLMethod
import core.model.puml.PUMLType

class MethodComparator {

    fun compareTypeMethdos(
        implementationClasses: List<PUMLType>,
        designClasses: List<PUMLType>,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implClassesMap = implementationClasses.associateBy { it.fullName }
        val designClassesMap = designClasses.associateBy { it.fullName }

        val correctClassesNames = implClassesMap.keys.intersect(designClassesMap.keys)

        correctClassesNames.forEach { correctClassName ->
            val correctImplClass = implClassesMap[correctClassName]
            val correctDesignClass = designClassesMap[correctClassName]

            if (correctImplClass != null && correctDesignClass != null) {
               deviations.addAll(checkMethod(correctImplClass, correctDesignClass))
            }

        }

        return deviations
    }

    private fun checkMethod(
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
        val distinct = deviations.distinct()
        return distinct
    }

    private fun checkDeviatingMethods(
        deviatingMethods: List<PUMLMethod>,
        implementationClass: PUMLType,
        designClass: PUMLType,
        type: DeviationType
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val deviatingMethodsMap = deviatingMethods.associateBy { it.name }
        val implementationClassMap = implementationClass.methods.associateBy { it.name }
        val designClassMap = designClass.methods.associateBy { it.name }

        deviatingMethodsMap.forEach { method ->
            val match = when (type) {
                DeviationType.ABSENCE -> implementationClassMap[method.key]
                DeviationType.UNEXPECTED -> designClassMap[method.key]
                else -> implementationClassMap[method.key]
            }
            match?.let {
                val deviationCauses = checkMethodDeviation(match, method.value) // todo fix
                deviations.add(
                    Deviation(
                        DeviationLevel.MIKRO,
                        DeviationArea.BEHAVIOR, // TODO behavior is bad wording
                        DeviationType.MISIMPLEMENTED,
                        listOf(designClass.name),
                        "Maybe wrong implemented method",
                        "Method ${method.value.name} in class ${designClass.name} is implemented incorrectly: $deviationCauses"
                    )
                )
            } ?: run {
                deviations.add( // wenn die methode immernoch nicht gefunden wird dann existiert sie nicht
                    Deviation(
                        DeviationLevel.MIKRO,
                        DeviationArea.BEHAVIOR, // TODO behavior is bad wording
                        type,
                        listOf(designClass.name),
                        "$type method",
                        "Method ${method.value.name} in class ${designClass.name} is $type"
                    )
                )
            }
        }
        return deviations
    }

    private fun checkMethodDeviation(
        implementationMethod: PUMLMethod,
        designMethod: PUMLMethod
    ): List<String> {
        val methodWarnings = mutableListOf<String>()
        if (implementationMethod.isAbstract && !designMethod.isAbstract) {
            methodWarnings.add("Method ${implementationMethod.name} is marked as abstract but should not be abstract according to the design") // TODO revise text
        } else if (!implementationMethod.isAbstract && designMethod.isAbstract) {
            methodWarnings.add("Method ${designMethod.name} should be abstract according to the design but it not in the impl.")
        }

        if (implementationMethod.visibility != designMethod.visibility) {
            methodWarnings.add(
                "Method ${implementationMethod.name} should have the Visbility ${designMethod.visibility} but has the " +
                        "visibility ${implementationMethod.visibility}"
            )
        }

        return methodWarnings
    }
}