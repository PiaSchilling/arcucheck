package core.api

interface CodeParser {
    /**
     * Parses the code located at the specified path into a PlantUML diagram (string representation).
     *
     * @param codePath the path to the code for which the diagram should be generated
     * @return a string containing a PlantUML diagram that represents the entire code
     */
    fun parseCode(codePath: String): String
}