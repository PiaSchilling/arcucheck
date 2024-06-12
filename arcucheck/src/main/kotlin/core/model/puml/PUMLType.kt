package core.model.puml

/**
 * Interface for PUMLClass and PUMLInterface to provide polymorphism functionality to those classes
 * "Type" serves as an umbrella term for "class" and "interface"
 */
interface PUMLType {
    val name: String
    val fullName: String get() = "${pumlPackage.fullName}.$name"
    val pumlPackage: PUMLPackage
    val methods: List<PUMLMethod>
}
