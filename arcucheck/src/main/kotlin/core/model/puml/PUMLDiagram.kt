package core.model.puml

data class PUMLDiagram(
    val sourcePath: String,
    val classes: List<PUMLClass>,
    val interfaces: List<PUMLInterface>,
    val relations: List<PUMLRelation>
)
