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

        val result = classDeviations + relationDeviations
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

    private fun checkUnexpectedAbsentClasses(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implClassesMap = implementationClasses.associateBy { it.name }
        val designClassesMap = designClasses.associateBy { it.name }

        val unexpectedClassNames = designClassesMap.keys.subtract(implClassesMap.keys)
        val absentClassNames = implClassesMap.keys.subtract(designClassesMap.keys)

        if (absentClassNames.isNotEmpty()) {
            val absentClasses = implClassesMap.filterKeys { it in absentClassNames }
            absentClasses.forEach { absentClass ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.PROPERTY,
                        DeviationType.ABSENCE,
                        listOf(absentClass.value.name),
                        "Missing class",
                        "Class \"${absentClass.value.name}\" is expected in the design but missing in the implementation."
                    )
                )
            }
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
                    )
                )
            }
        }
        return deviations
    }

    private fun comparePUMLRelations(
        implementationRelations: List<PUMLRelation>,
        designRelations: List<PUMLRelation>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val unexpectedRelations = designRelations.subtract(implementationRelations.toSet())
        val absentRelations = implementationRelations.subtract(designRelations.toSet())

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

    fun comparePUMLMethod(containingClass: PUMLClass, codeDiagramMethod: PUMLMethod, designDiagramMethod: PUMLMethod) {
        val differences = mutableListOf<String>()
        if (codeDiagramMethod.returnType != designDiagramMethod.returnType) {
            differences.add(
                "${containingClass.name}: Wrong return type for Method ${designDiagramMethod.name}." +
                        " Should be ${designDiagramMethod.returnType}, but is ${codeDiagramMethod.returnType}"
            )
        }
    }
}