package core.model

data class PUMLClass(
    val name: String,
    val fields: List<PUMLField>,
    val methods: List<PUMLMethod>,
    val isAbstract: Boolean
)
