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
        val constructorComparator = ConstructorComparator()

        val classDeviations = typesComparator.comparePUMLTypes(implementationDiagram.classes, designDiagram.classes)
        val interfaceDeviations =
            typesComparator.comparePUMLTypes(implementationDiagram.interfaces, designDiagram.interfaces)
        val relationDeviations =
            relationComparator.comparePUMLRelations(implementationDiagram.relations, designDiagram.relations)
        val packageDeviations =
            packageComparator.comparePUMLPackages(implementationDiagram.classes, designDiagram.classes)
        val classMethodDeviations =
            methodComparator.comparePUMLMethdos(implementationDiagram.classes, designDiagram.classes)
        val interfaceMethodDeviations =
            methodComparator.comparePUMLMethdos(implementationDiagram.interfaces, designDiagram.interfaces)
        val fieldDeviations = fieldComparator.comparePUMLFields(implementationDiagram.classes, designDiagram.classes)
        val constructorDeviations =
            constructorComparator.comparePUMLConstructors(implementationDiagram.classes, designDiagram.classes)
        val result =
            classDeviations + relationDeviations + packageDeviations + interfaceDeviations + classMethodDeviations + interfaceMethodDeviations + fieldDeviations + constructorDeviations
        if (result.isEmpty()) {
            println("No deviations between implementation and design found")
        } else {
            println(result)
        }
    }

}