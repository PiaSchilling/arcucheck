package core.model.puml

data class PUMLInterface(
    override val name: String,
    override val pumlPackage: PUMLPackage,
    override val methods: List<PUMLMethod>,
) : PUMLType
