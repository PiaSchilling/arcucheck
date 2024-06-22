package core.api

import core.model.deviation.Deviation
import core.model.puml.PUMLDiagram

interface PUMLComparator {
    /**
     * Compare an PlantUML diagram that represents the implementation to a PlantUML diagram that represents the design
     * to detect any deviations between them
     *
     * @param implementationDiagram PlantUML diagram that represents the implementation
     * @param designDiagram PlantUML diagram that represents the design
     * @return a list of all detected deviations
     */
    fun comparePUMLDiagrams(implementationDiagram: PUMLDiagram, designDiagram: PUMLDiagram): List<Deviation>
}