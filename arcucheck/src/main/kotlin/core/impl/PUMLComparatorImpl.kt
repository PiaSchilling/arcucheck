package core.impl

import core.model.puml.PUMLClass
import core.model.puml.PUMLDiagram
import core.model.puml.PUMLMethod
import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType

class PUMLComparatorImpl {

    fun comparePUMLDiagrams(codeDiagram: PUMLDiagram, designDiagram: PUMLDiagram) {
        val deviations = mutableListOf<Deviation>()

        val result = comparePUMLClasses(deviations, codeDiagram.classes, designDiagram.classes)
        println(result)
    }

    private fun comparePUMLClasses(
        deviations: MutableList<Deviation>,
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
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
                        absentClass.value,
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
                        unexpectedClass.value,
                        "Unexpected class",
                        "Class \"${unexpectedClass.value.name}\" is not expected in the design but present in the implementation."
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