package core.api

import core.model.puml.PUMLDiagram

interface PUMLMapper {
    /**
     * Maps the string representation of a PlantUML diagram into a PUMLDiagram model by extracting classes,
     * interfaces, methods, relationships, and other elements from the diagram.
     *
     * @param umlText the PlantUML diagram as plain text (string representation)
     * @return the PlantUML diagram as a PUMLDiagram model
     * // TODO comment params (diagram name optinal bc only design has it)
     */
    fun mapDiagram(sourcePath: String, umlText: String, diagramName: String?): PUMLDiagram
}