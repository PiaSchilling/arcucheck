package core.impl

import core.api.UMLMapper
import core.model.PUMLMethod
import core.model.Visibility

class UMLMapperImpl : UMLMapper {
}

fun main() {
    val text = """
        class de.hdm_stuttgart.editor.integration.EditorController {
        + <<Create>> EditorController(IEditorRepo,String)
        + void renderMarkdownToHtml(String)
        + StringProperty getHtmlStringProperty()
        - {static} Test privateMethod()
        # {abstract} Test protectedMethod(Test,Pia)
        }
    """.trimIndent()

    // Regex für den Klassennamen
    val classNamePattern = Regex("""class\s+([\w.]+)\s*\{""")
    val classNameMatch = classNamePattern.find(text)
    val className = classNameMatch?.groupValues?.get(1) ?: "Class name not found"
    println("Class Name: $className")

    // Regex for Methoden
    val methodPattern = Regex("""([+\-#])\s*(\{(?:static|abstract)?})?\s*((\w+)\s+(\w+)\((.*?)\))""")
    val methods = methodPattern.findAll(text)

    val pumlMethods = mutableListOf<PUMLMethod>()

    for (method in methods) {
        val methodVisibility = method.groupValues[1]
        val methodStaticAbstract = method.groupValues[2]
        val methodReturnType = method.groupValues[4]
        val methodName = method.groupValues[5]
        val methodParameters = method.groupValues[6]

        val isAbstract = methodStaticAbstract.contains("abstract")
        val isStatic = methodStaticAbstract.contains("static")

        pumlMethods.add(
            PUMLMethod(
                methodName,
                methodReturnType,
                methodParameters.split(","),
                Visibility.fromString(methodVisibility),
                isStatic,
                isAbstract
            )
        )
    }

    println("PUML Methods: $pumlMethods")

    // Regex for constructor
    val pattern = "([+\\-#])\\s*<<Create>>\\s+(\\w+)\\((.*?)\\)".toRegex()
    val matches = pattern.findAll(text)

    matches.forEach { matchResult ->
        val constructorSignature = matchResult.groupValues[0]
        val constructorName = matchResult.groupValues[2]
        val constructorParams = matchResult.groupValues[3]
        val constructorVisibility = matchResult.groupValues[1]
        println("Constructor Name: $constructorName")
        println("Constructor Parameters: $constructorParams")
        println("Constructor visibility: $constructorVisibility")
        println(constructorSignature)
    }


}