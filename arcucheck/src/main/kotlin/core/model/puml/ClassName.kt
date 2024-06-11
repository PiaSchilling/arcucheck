package core.model.puml


data class ClassName(val fullName:String, val isAbstract: Boolean){
    val className = fullName.substringAfterLast(".")
    val packageName =  fullName.substringBeforeLast(".")
}
