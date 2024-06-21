package core.impl

import core.impl.comparators.*
import core.model.deviation.Deviation
import core.model.puml.*

class PUMLComparatorImpl {

    fun comparePUMLDiagrams(implementationDiagram: PUMLDiagram, designDiagram: PUMLDiagram): List<Deviation> {
        val designDiagramPath = designDiagram.sourcePath
        val implPath = implementationDiagram.sourcePath

        // TODO extract from this method, Single resp prinzip!
        val typesComparator = TypesComparator(designDiagramPath, implPath)
        val relationComparator = RelationComparator(designDiagramPath, implPath)
        val packageComparator = PackageComparator(designDiagramPath, implPath)
        val methodComparator = MethodComparator(designDiagramPath, implPath)
        val fieldComparator = FieldComparator(designDiagramPath, implPath)
        val constructorComparator = ConstructorComparator(designDiagramPath, implPath)

        val classDeviations = typesComparator.comparePUMLTypes(implementationDiagram.classes, designDiagram.classes)
        val interfaceDeviations =
            typesComparator.comparePUMLTypes(implementationDiagram.interfaces, designDiagram.interfaces)
        val relationDeviations =
            relationComparator.comparePUMLRelations(implementationDiagram.relations, designDiagram.relations)
        val packageDeviations =
            packageComparator.comparePUMLPackages(
                implementationDiagram.classes + implementationDiagram.interfaces,
                designDiagram.classes + designDiagram.interfaces
            )
        val classMethodDeviations =
            methodComparator.comparePUMLMethdos(implementationDiagram.classes, designDiagram.classes)
        val interfaceMethodDeviations =
            methodComparator.comparePUMLMethdos(implementationDiagram.interfaces, designDiagram.interfaces)
        val fieldDeviations = fieldComparator.comparePUMLFields(implementationDiagram.classes, designDiagram.classes)
        val constructorDeviations =
            constructorComparator.comparePUMLConstructors(implementationDiagram.classes, designDiagram.classes)

        return classDeviations +
                relationDeviations +
                packageDeviations +
                interfaceDeviations +
                classMethodDeviations +
                interfaceMethodDeviations +
                fieldDeviations +
                constructorDeviations
    }

}