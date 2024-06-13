package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLRelation

class RelationComparator {

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