package core.model

data class PUMLClass (
    val name: String,
    val constructors: List<PUMLConstructor>,
    val fields: List<PUMLField>,
    val methods: List<PUMLMethod>,
    val isAbstract: Boolean
) : PUMLType
