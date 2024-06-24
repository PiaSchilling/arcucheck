package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationSubjectType
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLRelation

/**
 * Compares the design of PUMLClasses and PUMLInterfaces to the according implementation to detect any deviations
 *
 * @constructor just initializes fields, those are only necessary, so they can be added to the
 * output for more precise warnings
 *
 *
 * @param designDiagramPath the path to the diagram representing the design
 * @param implPath the path to the implementation which is compared to the design
 */
class RelationComparator(private val designDiagramPath: String, private val implPath: String) {

    /**
     * Compare the in the design expected relations to the in the implementation present relations and detect any deviations
     *
     * @param implementationRelations all relations present in the implementation
     * @param designRelations all relations expected by the design
     * @return a list of all detected deviations between design and implementation
     */
    fun comparePUMLRelations(
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
                        DeviationSubjectType.RELATION,
                        DeviationType.ABSENCE,
                        listOf(absentRelation.sourceClass, absentRelation.destinationClass),
                        "Absent relation",
                        "Relation of type ${absentRelation.relationType} between source class " +
                                "\"${absentRelation.sourceClass}\" and destination class \"${absentRelation.destinationClass}\" " +
                                "is expected according to the design but missing in the implementation.",
                        designDiagramPath,
                        implPath,
                    )
                )
            }
        }

        if (unexpectedRelations.isNotEmpty()) {
            unexpectedRelations.forEach { unexpectedRelation ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationSubjectType.RELATION,
                        DeviationType.UNEXPECTED,
                        listOf(unexpectedRelation.sourceClass, unexpectedRelation.destinationClass),
                        "Unexpected relation",
                        "Relation of type ${unexpectedRelation.relationType} between source class " +
                                "\"${unexpectedRelation.sourceClass}\" and destination class \"${unexpectedRelation.destinationClass}\" " +
                                "is not expected according to the design but present in the implementation.",
                        designDiagramPath,
                        implPath,
                    )
                )
            }
        }
        return deviations
    }
}