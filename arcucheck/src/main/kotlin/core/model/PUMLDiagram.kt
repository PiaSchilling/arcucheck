package core.model

data class PUMLDiagram(
    val name: String,
    val classes: List<PUMLClass>,
    val interfaces: List<PUMLInterface>
)
