package core.impl.comparators

import core.impl.WarningBuilder
import core.model.deviation.*
import core.model.puml.PUMLClass
import core.model.puml.PUMLType

class TypesComparator {

    /**
     * Compare the provided classes according to their existence and package placement.
     *
     * @param implementationClasses the classes as they exist in the implementation
     * @param designClasses the same classes as present in the design
     * @return a list of all detected deviations between design and implementation. Could either be the absence of a class,
     * an unexpected class or a class placed in the wrong package.
     */
    fun comparePUMLTypes(
        implementationClasses: List<PUMLType>,
        designClasses: List<PUMLType>,
    ): List<Deviation> {
        return checkTypes(implementationClasses, designClasses)
    }


    /**
     * Check if any types (classes or interfaces) are unexpected, absent or placed in the wrong package
     *
     * @param implementationClasses the classes/interfaces present in the implementation
     * @param designClasses the classes/interfaces expected by the design
     * @return a list of all detected deviations between the design and implementation classes/interfaces
     */
    private fun checkTypes(
        implementationClasses: List<PUMLType>,
        designClasses: List<PUMLType>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implClassesMap = implementationClasses.associateBy { it.fullName }
        val designClassesMap = designClasses.associateBy { it.fullName }

        val maybeAbsentClassesNames = designClassesMap.keys.subtract(implClassesMap.keys)
        val maybeUnexpectedClassesNames = implClassesMap.keys.subtract(designClassesMap.keys)

        if (maybeAbsentClassesNames.isNotEmpty()) {
            val maybeAbsentClasses = designClassesMap.filterKeys { it in maybeAbsentClassesNames }.values.toList()
            deviations.addAll(
                checkDeviatingTypes(
                    maybeAbsentClasses,
                    implementationClasses,
                    designClasses,
                    DeviationType.ABSENCE
                )
            )
        }
        if (maybeUnexpectedClassesNames.isNotEmpty()) {
            val maybeUnexpectedClasses = implClassesMap.filterKeys { it in maybeUnexpectedClassesNames }.values.toList()
            deviations.addAll(
                checkDeviatingTypes(
                    maybeUnexpectedClasses,
                    implementationClasses,
                    designClasses,
                    DeviationType.UNEXPECTED
                )
            )
        }
        return deviations.distinct()
    }

    /**
     * Checks for types (classes or interfaces) that are either absent (present in the design but not in the implementation) or are placed
     * in the incorrect package.
     *
     * @param deviatingClasses Classes or interfaces expected in the design but not found in the implementation, needing verification.
     * @param implementationClasses All classes or interfaces present in the implementation.
     * @return A list of detected deviations, categorized as "class/interface in wrong package" if the class/interface
     * should have been placed in another package according to the design, or "absent class/interface" if the class/interface
     * cannot be found anywhere in the implementation.
     */
    private fun checkDeviatingTypes(
        deviatingClasses: List<PUMLType>,
        implementationClasses: List<PUMLType>,
        designClasses: List<PUMLType>,
        deviationType: DeviationType,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        // Check classes only by using the class name without the package name
        val deviatingClassesMap = deviatingClasses.associateBy { it.name }
        val designClassesMap = designClasses.associateBy { it.name }
        val implClassesMap = implementationClasses.associateBy { it.name }

        var notFoundClasses = emptyMap<String, PUMLType>()
        if (deviationType == DeviationType.ABSENCE) {
            val absentClassesNames = deviatingClassesMap.keys.subtract(implClassesMap.keys)
            notFoundClasses = deviatingClassesMap.filterKeys { it in absentClassesNames }
        } else if (deviationType == DeviationType.UNEXPECTED) {
            val unexpectedClassNames = deviatingClassesMap.keys.subtract(designClassesMap.keys)
            notFoundClasses = deviatingClassesMap.filterKeys { it in unexpectedClassNames }
        }

        var foundClasses = emptyMap<String, PUMLType>()
        if (deviationType == DeviationType.ABSENCE) {
            val notFoundClassNames = deviatingClassesMap.keys.intersect(implClassesMap.keys)
            foundClasses = deviatingClassesMap.filterKeys { it in notFoundClassNames }
        }

        // If class still can not be found, it is absent
        notFoundClasses.forEach { type ->
            val subject = if (type.value is PUMLClass) DeviationSubject.CLASS else DeviationSubject.INTERFACE
            deviations.add(
                WarningBuilder.buildUnexpectedAbsentDeviation(
                    level = DeviationLevel.MAKRO,
                    area = DeviationArea.PROPERTY,
                    type = deviationType,
                    affectedClassesNames = listOf(type.value.name),
                    subject = subject,
                    subjectName = type.value.name,
                    classLocation = type.value.pumlPackage.fullName
                )
            )
        }

        // If class now can be found, it might be just placed in the wrong package (or two classes with same name exist)
        foundClasses.forEach { existingDesignClass ->
            val wrongImplClass = implClassesMap[existingDesignClass.key]
            val typeKeyword = if (wrongImplClass is PUMLClass) "Class" else "Interface"
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.MISIMPLEMENTED,
                    listOf(existingDesignClass.value.name),
                    "$typeKeyword maybe in wrong package",
                    "According to the design, the $typeKeyword \"${existingDesignClass.value.name}\" is expected " +
                            "in the package \"${existingDesignClass.value.pumlPackage.fullName}\", but it is absent in the " +
                            "implementation. Instead, a class with the same name is found in the package \"${wrongImplClass?.pumlPackage?.fullName}\". " +
                            "It may be in the wrong package."
                )
            )
        }
        return deviations
    }

}

