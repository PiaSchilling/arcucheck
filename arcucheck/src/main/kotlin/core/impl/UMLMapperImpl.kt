package core.impl

import core.api.UMLMapper

class UMLMapperImpl : UMLMapper {
}

fun main() {
    val text = """
        class de.hdm_stuttgart.editor.integration.EditorController {
        + <<Create>> EditorController(IEditorRepo,String)
        + void renderMarkdownToHtml(String)
        + StringProperty getHtmlStringProperty()
        - {static} Test privateMethod()
        # {abstract} Test protectedMethod()
        }
    """.trimIndent()

    // Regex für den Klassennamen
    val classNamePattern = Regex("""class\s+([\w.]+)\s*\{""")
    val classNameMatch = classNamePattern.find(text)
    val className = classNameMatch?.groupValues?.get(1) ?: "Class name not found"
    println("Class Name: $className")

    // Regex for Methoden
    val methodPattern = Regex("""([+\-#])\s*((\w+)\s+(\w+)(\(.*?\)))""")
    val methods = methodPattern.findAll(text)

    for (method in methods) {
        val visibilityModifier = method.groupValues[1]
        val methodName= method.groupValues[4]
        val methodParameters = method.groupValues[5]
        val methodReturnType = method.groupValues[3]
        println("Visibility: $visibilityModifier, " +
                "Return type: $methodReturnType, " +
                "MethodName: $methodName, " +
                "Params: $methodParameters")
    }

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