package control.api

interface Controller {
    /**
     * Execute the command for a single design PlantUML diagram
     *
     * @param implementationPath the path to the implementation
     * @param designPath the path to the design PlantUML diagram
     */
    fun onExecuteCommandSingleFile(implementationPath: String, designPath: String)

    /**
     * Execute the command for a directory of design puml diagrams
     *
     * @param directoryPath the path to the directory containing multiple design diagrams (note: the design diagrams
     * have to include the 'implementation_path= comment at the top to specify the related paths to the implementations)
     */
    fun onExecuteCommandDirectory(directoryPath: String)
}