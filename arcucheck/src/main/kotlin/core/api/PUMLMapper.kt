package core.api

import core.model.puml.PUMLDiagram

interface PUMLMapper {
    /**
     * Maps the string representation of a PlantUML diagram into a PUMLDiagram model by extracting classes,
     * interfaces, methods, relationships, and other elements from the diagram.
     *
     * @param sourcePath the path to the source of either the design .puml file or the implementation source code file
     * (only to provide better warning output with error location)
     * @param umlText the PlantUML diagram as plain text (string representation)
     * @return the PlantUML diagram as a PUMLDiagram model
     */
    fun mapDiagram(sourcePath: String, umlText: String): PUMLDiagram
}