package core.impl

import core.model.puml.PUMLClass
import core.model.puml.PUMLDiagram
import core.model.puml.PUMLMethod
import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLRelation

class PUMLComparatorImpl {

    fun comparePUMLDiagrams(implementationDiagram: PUMLDiagram, designDiagram: PUMLDiagram) {

        val classDeviations = comparePUMLClasses(implementationDiagram.classes, designDiagram.classes)
        val relationDeviations = comparePUMLRelations(implementationDiagram.relations, designDiagram.relations)
        val packageDeviations = checkUnexpectedAbsentPackages(implementationDiagram.classes, designDiagram.classes)

        val result = classDeviations + relationDeviations + packageDeviations
        if (result.isEmpty()) {
            println("No deviations between implementation and design found")
        } else {
            println(result)
        }
    }

    private fun comparePUMLClasses(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        // TODO add missing method checks etc.
        return checkUnexpectedAbsentClasses(implementationClasses, designClasses)
    }

    /**
     * Prüfen, ob eine Klasse, die im Design vorhanden ist aber mit ihrem full package identifier im code nicht gefunden
     * wurde evtl nur im falschen package ist oder komplett nicht vorhanden ist.
     *
     * @param notFoundCodeClasses
     * @param codeClasses
     * @return
     */ // TODO rename method, clean up for consistency
    private fun checkClassPackages(
        notFoundCodeClasses: List<PUMLClass>,
        codeClasses: List<PUMLClass>,
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        // Check classes only by using the class name without the package name
        val notFoundClassesMap = notFoundCodeClasses.associateBy { it.name }
        val codeClassesMap = codeClasses.associateBy { it.name }

        // Klassen, die auch mit "nur Klassennamen" (ohne package) nicht gefunden werden
        // findet klassen, die im design vorhanden sind aber nicht im code

        // If class still can not be found, it is absent
        val notFoundClassNames = notFoundClassesMap.keys.subtract(codeClassesMap.keys)
        val notFoundClasses = notFoundClassesMap.filterKeys { it in notFoundClassNames }
        notFoundClasses.forEach { absentClass ->
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
        val foundClassNames = notFoundClassesMap.keys.intersect(codeClassesMap.keys)
        val foundClasses = notFoundClassesMap.filterKeys { it in foundClassNames }

        foundClasses.forEach { existingClass ->
            val wrongClass = codeClassesMap[existingClass.key]
            deviations.add(
                Deviation(
                    DeviationLevel.MAKRO,
                    DeviationArea.PROPERTY,
                    DeviationType.ABSENCE,
                    listOf(existingClass.value.name),
                    "Class in wrong package",
                    "Class \"${existingClass.value.name}\" is expected to be placed in the package ${existingClass.value.pumlPackage.fullName}" +
                            " but is placed in the package ${wrongClass?.pumlPackage?.fullName}."
                )
            )
        }
        return deviations
    }

    private fun checkUnexpectedAbsentClasses(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implClassesMap = implementationClasses.associateBy { it.fullName }
        val designClassesMap = designClasses.associateBy { it.fullName }

        val notFoundClassNames = designClassesMap.keys.subtract(implClassesMap.keys)
        val unexpectedClassNames = implClassesMap.keys.subtract(designClassesMap.keys)

        if (notFoundClassNames.isNotEmpty()) {
            val notFoundClasses = designClassesMap.filterKeys { it in notFoundClassNames }.values.toList()
            deviations.addAll(checkClassPackages(notFoundClasses, implementationClasses))
        }
        if (unexpectedClassNames.isNotEmpty()) {
            val unexpectedClasses = designClassesMap.filterKeys { it in unexpectedClassNames }
            unexpectedClasses.forEach { unexpectedClass ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.PROPERTY,
                        DeviationType.UNEXPECTED,
                        listOf(unexpectedClass.value.name),
                        "Unexpected class",
                        "Class \"${unexpectedClass.value.name}\" is not expected in the design but present in the implementation."
                    ) // TODO add package name
                )
            }
        }
        return deviations
    }

    /**
     * Check for unexpected or absent packages
     *
     * @param implementationClasses
     * @param designClasses
     * @return
     */
    private fun checkUnexpectedAbsentPackages(
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
     * Check if classes are in the wrong package
     *
     * @param implementationClasses
     * @param designClasses
     * @return
     */
    private fun checkClassPackageStructure(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()


        return deviations
    }

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


    fun comparePUMLMethods(codeDiagramMethods: List<PUMLMethod>, designDiagramMethods: List<PUMLMethod>) {
        val codeMethodsMap = codeDiagramMethods.associateBy { it.name }
        val designMethodsMap = designDiagramMethods.associateBy { it.name }


    }

    fun comparePUMLMethod(
        containingClass: PUMLClass,
        codeDiagramMethod: PUMLMethod,
        designDiagramMethod: PUMLMethod
    ) {
        val differences = mutableListOf<String>()
        if (codeDiagramMethod.returnType != designDiagramMethod.returnType) {
            differences.add(
                "${containingClass.name}: Wrong return type for Method ${designDiagramMethod.name}." +
                        " Should be ${designDiagramMethod.returnType}, but is ${codeDiagramMethod.returnType}"
            )
        }
    }
}