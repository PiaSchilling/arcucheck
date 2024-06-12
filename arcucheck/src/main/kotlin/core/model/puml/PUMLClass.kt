package core.model.puml

data class PUMLClass(
    override val name: String,
    override val pumlPackage: PUMLPackage,
    val constructors: List<PUMLConstructor>,
    val fields: List<PUMLField>,
    override val methods: List<PUMLMethod>,
    val isAbstract: Boolean
) : PUMLType
