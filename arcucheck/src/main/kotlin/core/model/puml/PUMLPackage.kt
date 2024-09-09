package core.model.puml

data class PUMLPackage(val fullName: String){
    val nestedNames = fullName.split(".")
}
