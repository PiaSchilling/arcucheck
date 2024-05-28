package core.model

data class PUMLMethod(
    val name: String,
    val returnType: String,
    val parameterTypes: List<String>,
    val visibility: Visibility,
    val isStatic: Boolean,
    val isAbstract: Boolean
)
