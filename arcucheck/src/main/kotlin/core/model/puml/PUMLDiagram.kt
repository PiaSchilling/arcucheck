package core.model.puml

data class PUMLDiagram(
    val sourcePath: String, // TODO fix: for design diagrams its the name and for the impl diagrams its the path ...
    // TODO #1 add path for both, and for design add also name of file, then add var for "fullName" o.ä.
    val diagramName: String?,
    val classes: List<PUMLClass>,
    val interfaces: List<PUMLInterface>,
    val relations: List<PUMLRelation>
)
