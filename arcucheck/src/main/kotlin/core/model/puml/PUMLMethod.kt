package core.model.puml

data class PUMLMethod(
    val name: String,
    val returnType: String,
    val parameterTypes: List<String>,
    val visibility: Visibility,
    val isStatic: Boolean,
    val isAbstract: Boolean
){
    val signature = "$returnType $name(${parameterTypes})"
}
