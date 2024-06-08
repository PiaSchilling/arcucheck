package core.model.PUML

data class PUMLDiagram(
    val name: String,
    val classes: List<PUMLClass>,
    val interfaces: List<PUMLInterface>,
    val relations: List<PUMLRelation>
)
