package core.model.puml

data class PUMLClass(
    val name: String,
    val pumlPackage: PUMLPackage,
    val constructors: List<PUMLConstructor>,
    val fields: List<PUMLField>,
    val methods: List<PUMLMethod>,
    val isAbstract: Boolean
) : PUMLType{
    val fullName = pumlPackage.fullName + "." + name
}
