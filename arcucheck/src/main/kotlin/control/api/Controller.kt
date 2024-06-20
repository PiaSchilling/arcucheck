package control.api

interface Controller {
    /**
     * Execute the command for a single design puml diagram
     *
     * @param args containing the path to the single design puml diagram and the code // todo adjust once 2nd parameter fällt weg
     */
    fun onExecuteCommandSingleFile(args: List<String>)

    /**
     * Execute the command for a directory of design puml diagrams
     *
     * @param directoryPath the path to the design puml diagram directory
     */
    fun onExecuteCommandDirectory(directoryPath: String)
}