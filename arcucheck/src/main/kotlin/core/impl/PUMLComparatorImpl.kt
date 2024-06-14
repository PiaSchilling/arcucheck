package core.impl

import core.impl.comparators.*
import core.model.puml.*

class PUMLComparatorImpl {

    fun comparePUMLDiagrams(implementationDiagram: PUMLDiagram, designDiagram: PUMLDiagram) {

        val typesComparator = TypesComparator()
        val relationComparator = RelationComparator()
        val packageComparator = PackageComparator()
        val methodComparator = MethodComparator()
        val fieldComparator = FieldComparator()

        val classDeviations = typesComparator.comparePUMLTypes(implementationDiagram.classes, designDiagram.classes)
        val interfaceDeviations =
            typesComparator.comparePUMLTypes(implementationDiagram.interfaces, designDiagram.interfaces)
        val relationDeviations =
            relationComparator.comparePUMLRelations(implementationDiagram.relations, designDiagram.relations)
        val packageDeviations =
            packageComparator.comparePUMLPackages(implementationDiagram.classes, designDiagram.classes)
        val methodDeviations = methodComparator.comparePUMLMethdos(implementationDiagram.classes, designDiagram.classes)
        val fieldDeviations = fieldComparator.comparePUMLFields(implementationDiagram.classes, designDiagram.classes)

        val result =
            classDeviations + relationDeviations + packageDeviations + interfaceDeviations + methodDeviations + fieldDeviations
        if (result.isEmpty()) {
            println("No deviations between implementation and design found")
        } else {
            println(result)
        }
    }

}