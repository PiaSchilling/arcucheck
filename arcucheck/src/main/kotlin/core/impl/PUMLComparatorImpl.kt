package core.impl

import core.impl.comparators.PackageComparator
import core.impl.comparators.RelationComparator
import core.impl.comparators.TypesComparator
import core.model.puml.*

class PUMLComparatorImpl {

    fun comparePUMLDiagrams(implementationDiagram: PUMLDiagram, designDiagram: PUMLDiagram) {

        val typesComparator = TypesComparator()
        val relationComparator = RelationComparator()
        val packageComparator = PackageComparator()

        val classDeviations = typesComparator.comparePUMLTypes(implementationDiagram.classes, designDiagram.classes)
        val interfaceDeviations =
            typesComparator.comparePUMLTypes(implementationDiagram.interfaces, designDiagram.interfaces)
        val relationDeviations =
            relationComparator.comparePUMLRelations(implementationDiagram.relations, designDiagram.relations)
        val packageDeviations =
            packageComparator.comparePUMLPackages(implementationDiagram.classes, designDiagram.classes)
        //  val methodDeviations = compareTypeMethdos()

        val result = classDeviations + relationDeviations + packageDeviations + interfaceDeviations
        if (result.isEmpty()) {
            println("No deviations between implementation and design found")
        } else {
            println(result)
        }
    }

}