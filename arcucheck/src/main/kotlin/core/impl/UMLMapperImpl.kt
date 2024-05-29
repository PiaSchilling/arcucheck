package core.impl

import core.api.UMLMapper

class UMLMapperImpl : UMLMapper {
}

fun main() {
    val text = """
        class de.hdm_stuttgart.editor.integration.EditorController {
        + <<Create>> EditorController(IEditorRepo)
        + void renderMarkdownToHtml(String)
        + StringProperty getHtmlStringProperty()
        - Test private()
        # Test protected()
        }
    """.trimIndent()

    // Regex für den Klassennamen
    val classNamePattern = Regex("""class\s+([\w.]+)\s*\{""")
    val classNameMatch = classNamePattern.find(text)
    val className = classNameMatch?.groupValues?.get(1) ?: "Class name not found"
    println("Class Name: $className")

    // Regex für Konstruktoren und Methoden
    val methodPattern = Regex("""([+\-#])\s*(\w+\s+\w+\(.*?\))""")
    val methods = methodPattern.findAll(text)

    for (method in methods) {
        val visibilityModifier = method.groupValues[1]
        val methodSignature = method.groupValues[2]
        println("Visibility: $visibilityModifier, Method: $methodSignature")
    }
}