package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLMethod
import core.model.puml.PUMLType

class MethodComparator {

    private fun compareTypeMethdos(
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        return deviations
    }

    private fun checkMethod(
        implementationClass: PUMLType,
        designClass: PUMLType,
    ): List<Deviation> {

        val maybeAbsentMethods = designClass.methods.subtract(implementationClass.methods.toSet())
        val maybeUnexpectedMethods = implementationClass.methods.subtract(designClass.methods.toSet())

        val absentDeviations = checkMaybeAbsentMethods(maybeAbsentMethods.toList(), implementationClass, designClass)

        return absentDeviations
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

    private fun checkMaybeAbsentMethods(
        maybeAbsentMethods: List<PUMLMethod>,
        implementationClass: PUMLType,
        designClass: PUMLType
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val maybeAbsentMethodsMap = maybeAbsentMethods.associateBy { it.name }
        val implementationClassMap = implementationClass.methods.associateBy { it.name }

        maybeAbsentMethodsMap.forEach { method ->
            val match = implementationClassMap[method.key]
            match?.let {

                val deviationCauses = checkMethodDeviation(match, method.value)
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
                        DeviationType.ABSENCE,
                        listOf(designClass.name),
                        "Absent method",
                        "Method ${method.value.name} in class ${designClass.name} is expected according to the design " +
                                "but is missing in the implementation."
                    )
                )
            }
        }
        return deviations
    }
}