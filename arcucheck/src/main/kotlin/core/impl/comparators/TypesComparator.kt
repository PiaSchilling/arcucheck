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
            deviations.addAll(checkMaybeAbsentTypes(maybeAbsentClasses, implementationClasses))
        }
        if (maybeUnexpectedClassesNames.isNotEmpty()) {
            val maybeUnexpectedClasses = implClassesMap.filterKeys { it in maybeUnexpectedClassesNames }.values.toList()
            deviations.addAll(checkMaybeUnexpectedClasses(maybeUnexpectedClasses, designClasses))
        }
        return deviations.distinct()
    }

    /**
     * Checks for types (classes or interfaces) that are either absent (present in the design but not in the implementation) or are placed
     * in the incorrect package.
     *
     * @param maybeAbsentClasses Classes or interfaces expected in the design but not found in the implementation, needing verification.
     * @param implementationClasses All classes or interfaces present in the implementation.
     * @return A list of detected deviations, categorized as "class/interface in wrong package" if the class/interface
     * should have been placed in another package according to the design, or "absent class/interface" if the class/interface
     * cannot be found anywhere in the implementation.
     */
    private fun checkMaybeAbsentTypes(
        maybeAbsentClasses: List<PUMLType>,
        implementationClasses: List<PUMLType>,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        // Check classes only by using the class name without the package name
        val maybeAbsentClassesMap = maybeAbsentClasses.associateBy { it.name }
        val codeClassesMap = implementationClasses.associateBy { it.name }

        // If class still can not be found, it is absent
        val absentClassesNames = maybeAbsentClassesMap.keys.subtract(codeClassesMap.keys)
        val absentClasses = maybeAbsentClassesMap.filterKeys { it in absentClassesNames }
        absentClasses.forEach { absentClass ->
            val subject = if (absentClass.value is PUMLClass) DeviationSubject.CLASS else DeviationSubject.INTERFACE
            deviations.add(
                WarningBuilder.buildUnexpectedAbsentDeviation(
                    level = DeviationLevel.MAKRO,
                    area = DeviationArea.PROPERTY,
                    type = DeviationType.ABSENCE,
                    affectedClassesNames = listOf(absentClass.value.name),
                    subject = subject,
                    subjectName = absentClass.value.name,
                    classLocation = absentClass.value.pumlPackage.fullName
                )
            )
        }

        // If class now can  be found, it is just placed in the wrong package
        val foundClassNames = maybeAbsentClassesMap.keys.intersect(codeClassesMap.keys)
        val foundClasses = maybeAbsentClassesMap.filterKeys { it in foundClassNames }

        // TODO extract duplicate code -> if description of misimplemented deviation is altered the logic changes -> PROBLEM!
        foundClasses.forEach { existingDesignClass ->
            val wrongImplClass = codeClassesMap[existingDesignClass.key]
            val typeKeyword = if (wrongImplClass is PUMLClass) "Class" else "Interface"
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.MISIMPLEMENTED,
                    listOf(existingDesignClass.value.name),
                    "$typeKeyword maybe in wrong package",
                    "$typeKeyword \"${existingDesignClass.value.name}\" is absent in the package " +
                            "\"${existingDesignClass.value.pumlPackage.fullName}\". However, the package " +
                            "\"${wrongImplClass?.pumlPackage?.fullName}\" contains a class with the same name. It might be placed in the wrong package."
                )
            )
        }
        return deviations
    }

    /**
     * Checks for types (classes or interfaces) that are either unexpected (present in the implementation but not in the
     * design) or are placed in the incorrect package.
     *
     * @param maybeUnexpectedClasses Classes/interfaces found in the implementation but not in the design, needing verification.
     * @param designClasses All classes/interfaces expected by the design.
     * @return A list of detected deviations, categorized as "class/interface in wrong package" if the class/interface
     * should have been placed in another package according to the design, or "unexpected class/interface" if the class/interface
     * cannot be found anywhere in the design.
     */
    private fun checkMaybeUnexpectedClasses(
        maybeUnexpectedClasses: List<PUMLType>,
        designClasses: List<PUMLType>,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        // Check classes only by using the class name without the package name
        val maybeUnexpectedClassesMap = maybeUnexpectedClasses.associateBy { it.name }
        val designClassesMap = designClasses.associateBy { it.name }

        // If class still can not be found, it is unexpected
        val unexpectedClassesNames = maybeUnexpectedClassesMap.keys.subtract(designClassesMap.keys)
        val unexpectedClasses = maybeUnexpectedClassesMap.filterKeys { it in unexpectedClassesNames }
        unexpectedClasses.forEach { unexpectedClass ->
            val subject = if (unexpectedClass.value is PUMLClass) DeviationSubject.CLASS else DeviationSubject.INTERFACE
            deviations.add(
                WarningBuilder.buildUnexpectedAbsentDeviation(
                    level = DeviationLevel.MAKRO,
                    area = DeviationArea.PROPERTY,
                    type = DeviationType.UNEXPECTED,
                    affectedClassesNames = listOf(unexpectedClass.value.name),
                    subject = subject,
                    subjectName = unexpectedClass.value.name,
                    classLocation = unexpectedClass.value.pumlPackage.fullName
                )
            )
        }

        // If class now can  be found, it might be placed in the wrong package
        val foundClassesNames = maybeUnexpectedClassesMap.keys.intersect(designClassesMap.keys)
        val foundClasses = maybeUnexpectedClassesMap.filterKeys { it in foundClassesNames }

        foundClasses.forEach { existingImplClass ->
            val correctDesignClass = designClassesMap[existingImplClass.key]
            val typeKeyword = if (correctDesignClass is PUMLClass) "Class" else "Interface"
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.MISIMPLEMENTED,
                    listOf(existingImplClass.value.name),
                    "$typeKeyword maybe in wrong package",
                    "$typeKeyword \"${existingImplClass.value.name}\" is absent in the package " +
                            "\"${correctDesignClass?.pumlPackage?.fullName}\". However, the package " +
                            "\"${existingImplClass.value.pumlPackage.fullName}\" contains a class with the same name. It might be placed in the wrong package."
                )
            )
        }
        return deviations

    }

}

