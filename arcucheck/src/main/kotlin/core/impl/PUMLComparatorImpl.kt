package core.impl

import core.model.puml.PUMLClass
import core.model.puml.PUMLDiagram
import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLRelation
import core.model.puml.PUMLType

class PUMLComparatorImpl {

    fun comparePUMLDiagrams(implementationDiagram: PUMLDiagram, designDiagram: PUMLDiagram) {

        val classDeviations = comparePUMLTypes(implementationDiagram.classes, designDiagram.classes, true)
        val interfaceDeviations = comparePUMLTypes(implementationDiagram.interfaces, designDiagram.interfaces, false)
        val relationDeviations = comparePUMLRelations(implementationDiagram.relations, designDiagram.relations)
        val packageDeviations = comparePUMLPackages(implementationDiagram.classes, designDiagram.classes)

        val result = classDeviations + relationDeviations + packageDeviations + interfaceDeviations
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
    private fun comparePUMLTypes(
        implementationClasses: List<PUMLType>,
        designClasses: List<PUMLType>,
        isClass: Boolean
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        if (isClass) {
            val impl = implementationClasses.filterIsInstance<PUMLClass>()
            val design = designClasses.filterIsInstance<PUMLClass>()
            deviations.addAll(checkClassSignatures(impl, design))
        }

        return deviations + checkTypesExistence(implementationClasses, designClasses)
    }

    /**
     * Check if class signatures have any deviations (abstract modifier missing)
     *
     * @param implementationClasses the classes present in the implementation
     * @param designClasses the classes expected by the design
     * @return a list of all detected deviations between the design and implementation classes
     */
    private fun checkClassSignatures(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implClassesMap = implementationClasses.associateBy { it.fullName }

        designClasses.forEach { designClass ->
            val match = implClassesMap[designClass.fullName]
            match?.let {
                if (designClass.isAbstract && !it.isAbstract) {
                    deviations.add(
                        Deviation(
                            DeviationLevel.MAKRO,
                            DeviationArea.PROPERTY,
                            DeviationType.MISIMPLEMENTED,
                            listOf(it.name),
                            "Missing abstract modifier",
                            "Class \"${it.name}\" is expected to be marked as abstract according to the design but it not in the implementation."
                        )
                    )
                } else if (!designClass.isAbstract && it.isAbstract) {
                    deviations.add(
                        Deviation(
                            DeviationLevel.MAKRO,
                            DeviationArea.PROPERTY,
                            DeviationType.MISIMPLEMENTED,
                            listOf(it.name),
                            "Unexpected abstract modifier",
                            "Class \"${it.name}\" is marked as abstract in the implementation but is not expected to be abstract according to the design."
                        )
                    )
                }
            }
        }
        return deviations
    }

    /**
     * Check if any types (classes or interfaces) are unexpected, absent or placed in the wrong package
     *
     * @param implementationClasses the classes/interfaces present in the implementation
     * @param designClasses the classes/interfaces expected by the design
     * @return a list of all detected deviations between the design and implementation classes/interfaces
     */
    private fun checkTypesExistence(
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
            val typeKeyword = if (absentClass.value is PUMLClass) "class" else "interface"
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.ABSENCE,
                    listOf(absentClass.value.name),
                    "Absent $typeKeyword",
                    "$typeKeyword \"${absentClass.value.name}\" is expected in the design but missing in the implementation." // TODO extract string to some kind of error message builder
                )
            )
        }

        // If class now can  be found, it is just placed in the wrong package
        val foundClassNames = maybeAbsentClassesMap.keys.intersect(codeClassesMap.keys)
        val foundClasses = maybeAbsentClassesMap.filterKeys { it in foundClassNames }

        // TODO extract duplicate code
        foundClasses.forEach { existingClass ->
            val wrongClass = codeClassesMap[existingClass.key]
            val typeKeyword = if (wrongClass is PUMLClass) "Class" else "Interface"
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.MISIMPLEMENTED,
                    listOf(existingClass.value.name),
                    "$typeKeyword in wrong package", // TODO maybe add "maybe" keyword, es könnte ja immernoch sein, dass es klassen mit dem gleichen namen gibt
                    "$typeKeyword \"${existingClass.value.name}\" is expected to be placed in the package ${existingClass.value.pumlPackage.fullName}" +
                            " but is placed in the package ${wrongClass?.pumlPackage?.fullName}."
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
            val typeKeyword = if (unexpectedClass.value is PUMLClass) "class" else "interface"
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.UNEXPECTED,
                    listOf(unexpectedClass.value.name),
                    "Unexpected $typeKeyword",
                    "$typeKeyword \"${unexpectedClass.value.name}\" is not expected in the design but present in the implementation."
                )
            )
        }

        // If class now can  be found, it is just placed in the wrong package
        val foundClassesNames = maybeUnexpectedClassesMap.keys.intersect(designClassesMap.keys)
        val foundClasses = maybeUnexpectedClassesMap.filterKeys { it in foundClassesNames }

        foundClasses.forEach { existingClass ->
            val correctClass = designClassesMap[existingClass.key]
            val typeKeyword = if (correctClass is PUMLClass) "Class" else "Interface"
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.MISIMPLEMENTED,
                    listOf(existingClass.value.name),
                    "$typeKeyword in wrong package",
                    "$typeKeyword \"${existingClass.value.name}\" is expected to be placed in the package ${correctClass?.pumlPackage?.fullName}" +
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