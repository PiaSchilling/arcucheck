package core.impl

import core.model.puml.PUMLClass
import core.model.puml.PUMLDiagram
import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLRelation

class PUMLComparatorImpl {

    fun comparePUMLDiagrams(implementationDiagram: PUMLDiagram, designDiagram: PUMLDiagram) {

        val classDeviations = comparePUMLClasses(implementationDiagram.classes, designDiagram.classes)
        val relationDeviations = comparePUMLRelations(implementationDiagram.relations, designDiagram.relations)
        val packageDeviations = comparePUMLPackages(implementationDiagram.classes, designDiagram.classes)

        val result = classDeviations + relationDeviations + packageDeviations
        if (result.isEmpty()) {
            println("No deviations between implementation and design found")
        } else {
            println(result)
        }
    }

    /**
     * TODO (compare PUML classes regarding to thier existence (makro level) and implementation (mikro level)
     * @param implementationClasses
     * @param designClasses
     * @return
     */
    private fun comparePUMLClasses(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        // TODO add missing method checks etc.
        return checkClassesExistence(implementationClasses, designClasses)
    }

    /**
     * Check if any classes are unexpected, absent or placed in the wrong package
     *
     * @param implementationClasses the classes present in the implementation
     * @param designClasses the classes expected by the design
     * @return a list of all detected deviations between the design and implementation classes
     */
    private fun checkClassesExistence(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implClassesMap = implementationClasses.associateBy { it.fullName }
        val designClassesMap = designClasses.associateBy { it.fullName }

        val maybeAbsentClassesNames = designClassesMap.keys.subtract(implClassesMap.keys)
        val maybeUnexpectedClassesNames = implClassesMap.keys.subtract(designClassesMap.keys)

        if (maybeAbsentClassesNames.isNotEmpty()) {
            val maybeAbsentClasses = designClassesMap.filterKeys { it in maybeAbsentClassesNames }.values.toList()
            deviations.addAll(checkMaybeAbsentClasses(maybeAbsentClasses, implementationClasses))
        }
        if (maybeUnexpectedClassesNames.isNotEmpty()) {
            val maybeUnexpectedClasses = implClassesMap.filterKeys { it in maybeUnexpectedClassesNames }.values.toList()
            deviations.addAll(checkMaybeUnexpectedClasses(maybeUnexpectedClasses, designClasses))
        }
        return deviations.distinct()
    }

    /**
     * Checks for classes that are either absent (present in the design but not in the implementation) or are placed
     * in the incorrect package.
     *
     * @param maybeAbsentClasses Classes expected in the design but not found in the implementation, needing verification.
     * @param implementationClasses All classes present in the implementation.
     * @return A list of detected deviations, categorized as "class in wrong package" if the class should have been placed
     * in another package according to the design, or "absent class" if the class cannot be found anywhere in the implementation.
     */
    private fun checkMaybeAbsentClasses(
        maybeAbsentClasses: List<PUMLClass>,
        implementationClasses: List<PUMLClass>,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        // Check classes only by using the class name without the package name
        val maybeAbsentClassesMap = maybeAbsentClasses.associateBy { it.name }
        val codeClassesMap = implementationClasses.associateBy { it.name }

        // If class still can not be found, it is absent
        val absentClassesNames = maybeAbsentClassesMap.keys.subtract(codeClassesMap.keys)
        val absentClasses = maybeAbsentClassesMap.filterKeys { it in absentClassesNames }
        absentClasses.forEach { absentClass ->
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.ABSENCE,
                    listOf(absentClass.value.name),
                    "Absent class",
                    "Class \"${absentClass.value.name}\" is expected in the design but missing in the implementation."
                )
            )
        }

        // If class now can  be found, it is just placed in the wrong package
        val foundClassNames = maybeAbsentClassesMap.keys.intersect(codeClassesMap.keys)
        val foundClasses = maybeAbsentClassesMap.filterKeys { it in foundClassNames }

        // TODO extract duplicate code
        foundClasses.forEach { existingClass ->
            val wrongClass = codeClassesMap[existingClass.key]
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.MISIMPLEMENTED,
                    listOf(existingClass.value.name),
                    "Class in wrong package",
                    "Class \"${existingClass.value.name}\" is expected to be placed in the package ${existingClass.value.pumlPackage.fullName}" +
                            " but is placed in the package ${wrongClass?.pumlPackage?.fullName}."
                )
            )
        }
        return deviations
    }

    /**
     * Checks for classes that are either unexpected (present in the implementation but not in the design) or are placed
     * in the incorrect package.
     *
     * @param maybeUnexpectedClasses Classes found in the implementation but not in the design, needing verification.
     * @param designClasses All classes expected by the design.
     * @return A list of detected deviations, categorized as "class in wrong package" if the class should have been placed
     * in another package according to the design, or "unexpected class" if the class cannot be found anywhere in the design.
     */
    private fun checkMaybeUnexpectedClasses(
        maybeUnexpectedClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        // Check classes only by using the class name without the package name
        val maybeUnexpectedClassesMap = maybeUnexpectedClasses.associateBy { it.name }
        val designClassesMap = designClasses.associateBy { it.name }

        // If class still can not be found, it is unexpected
        val unexpectedClassesNames = maybeUnexpectedClassesMap.keys.subtract(designClassesMap.keys)
        val unexpectedClasses = maybeUnexpectedClassesMap.filterKeys { it in unexpectedClassesNames }
        unexpectedClasses.forEach { unexpectedClass ->
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.UNEXPECTED,
                    listOf(unexpectedClass.value.name),
                    "Unexpected class",
                    "Class \"${unexpectedClass.value.name}\" is not expected in the design but present in the implementation."
                )
            )
        }

        // If class now can  be found, it is just placed in the wrong package
        val foundClassesNames = maybeUnexpectedClassesMap.keys.intersect(designClassesMap.keys)
        val foundClasses = maybeUnexpectedClassesMap.filterKeys { it in foundClassesNames }

        foundClasses.forEach { existingClass ->
            val correctClass = designClassesMap[existingClass.key]
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.MISIMPLEMENTED,
                    listOf(existingClass.value.name),
                    "Class in wrong package",
                    "Class \"${existingClass.value.name}\" is expected to be placed in the package ${correctClass?.pumlPackage?.fullName}" +
                            " but is placed in the package ${existingClass.value.pumlPackage.fullName}."
                )
            )
        }
        return deviations

    }

    /**
     * Check if any packages are absent or unexpected
     *
     * @param implementationClasses the classes present in the implementation (classes contain their respective package)
     * @param designClasses the classes expected by the design (classes contain their respective package)
     * @return a list of all detected deviations between the design and implementation packages
     */
    private fun comparePUMLPackages(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implPackages = implementationClasses.map { pumlClass -> pumlClass.pumlPackage }.distinct()
        val designPackages = designClasses.map { pumlClass -> pumlClass.pumlPackage }.distinct()

        val absentPackages = designPackages.subtract(implPackages.toSet())
        val unexpectedPackages = implPackages.subtract(designPackages.toSet())


        if (absentPackages.isNotEmpty()) {
            absentPackages.forEach { absentPackage ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.PROPERTY,
                        DeviationType.ABSENCE,
                        listOf(absentPackage.fullName),
                        "Missing package",
                        "Package \"${absentPackage.fullName}\" is expected in the design but missing in the implementation."
                    )
                )
            }
        }
        if (unexpectedPackages.isNotEmpty()) {
            unexpectedPackages.forEach { unexpectedPackage ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.PROPERTY,
                        DeviationType.UNEXPECTED,
                        listOf(unexpectedPackage.fullName),
                        "Unexpected package",
                        "Package \"${unexpectedPackage.fullName}\" is not expected in the design but present in the implementation."
                    )
                )
            }
        }

        return deviations
    }

    /**
     * Compare the in the design expected relations to the in the implementation present relations and detect any deviations
     *
     * @param implementationRelations all relations present in the implementation
     * @param designRelations all relations expected by the design
     * @return a list of all detected deviations between design and implementation
     */
    private fun comparePUMLRelations(
        implementationRelations: List<PUMLRelation>,
        designRelations: List<PUMLRelation>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val absentRelations = designRelations.subtract(implementationRelations.toSet())
        val unexpectedRelations = implementationRelations.subtract(designRelations.toSet())

        if (absentRelations.isNotEmpty()) {
            absentRelations.forEach { absentRelation ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.RELATION,
                        DeviationType.ABSENCE,
                        listOf(absentRelation.sourceClass, absentRelation.destinationClass),
                        "Absent relation",
                        "Relation of type ${absentRelation.relationType} between source class " +
                                "\"${absentRelation.sourceClass}\" and destination class \"${absentRelation.destinationClass}\" " +
                                "is expected in the design but missing in the implementation."
                    )
                )
            }
        }

        if (unexpectedRelations.isNotEmpty()) {
            unexpectedRelations.forEach { unexpectedRelation ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.RELATION,
                        DeviationType.UNEXPECTED,
                        listOf(unexpectedRelation.sourceClass, unexpectedRelation.destinationClass),
                        "Unexpected relation",
                        "Relation of type ${unexpectedRelation.relationType} between source class " +
                                "\"${unexpectedRelation.sourceClass}\" and destination class \"${unexpectedRelation.destinationClass}\" " +
                                "is not expected in the design but present in the implementation."
                    )
                )
            }
        }
        return deviations
    }
}