package core.impl

import core.api.UMLMapper
import core.model.*

class UMLMapperImpl : UMLMapper {

    fun mapDiagram(umlText: String): PUMLDiagram {
        val className = mapClassNames(umlText)
        val constructors = mapConstructors(umlText)
        val methods = mapMethods(umlText)
        val fields = mapFields(umlText)

        println("classname: $className")
        println("constructors: $constructors")
        println("methods: $methods")
        println("fields: $fields")

        return PUMLDiagram("Test")
    }

    private fun mapFields(umlText: String): List<PUMLField> {
        val pumlFields = mutableListOf<PUMLField>()

        val fieldPattern = Regex(
            """([+\-#])\s*(\{(?:static)?})?\s*(\w+)\s+(\w+)(?!${'$'}\()(?!\()${'$'}""",
            RegexOption.MULTILINE
        )
        val fields = fieldPattern.findAll(umlText)

        fields.forEach { field ->
            val fieldVisibility = field.groupValues[1]
            val fieldStatic = field.groupValues[2]
            val fieldDataType = field.groupValues[3]
            val fieldName = field.groupValues[4]

            val isStatic = fieldStatic.contains("static")

            print("test")

            pumlFields.add(
                PUMLField(fieldName, fieldDataType, Visibility.fromString(fieldVisibility), isStatic)
            )

        }
        return pumlFields
    }

    private fun mapMethods(umlText: String): List<PUMLMethod> {
        val pumlMethods = mutableListOf<PUMLMethod>()

        val methodPattern = Regex("""([+\-#])\s*(\{(?:static|abstract)?})?\s*((\w+)\s+(\w+)\((.*?)\))""")
        val methods = methodPattern.findAll(umlText)

        methods.forEach { method ->
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
        return pumlMethods
    }

    private fun mapClassNames(umlText: String): List<String> {
        val pumlClassNames = mutableListOf<String>()
        // TODO check if splitting up class name and package names is necessary
        val classNamePattern = Regex("""class\s+([\w.]+)\s*\{""")
        val classNameMatches = classNamePattern.findAll(umlText)

         classNameMatches.forEach {
            matchResult -> pumlClassNames.add(matchResult.groupValues[1])
        }
        return pumlClassNames
    }

    private fun mapConstructors(umlText: String): List<PUMLConstructor> {
        val pumlConstructors = mutableListOf<PUMLConstructor>()

        val pattern = Regex("([+\\-#])\\s*<<Create>>\\s+(\\w+)\\((.*?)\\)")
        val matches = pattern.findAll(umlText)

        matches.forEach { matchResult ->
            val constructorParams = matchResult.groupValues[3]
            val constructorVisibility = matchResult.groupValues[1]

            pumlConstructors.add(
                PUMLConstructor(constructorParams.split(","), Visibility.fromString(constructorVisibility))
            )
        }

        return pumlConstructors
    }
}

fun main() {
    val text = """
        class de.hdm_stuttgart.editor.integration.EditorController {
        + String id
        - Int number
        # {static} Double counter
        + <<Create>> EditorController(IEditorRepo,String)
        + void renderMarkdownToHtml(String)
        + StringProperty getHtmlStringProperty()
        - {static} Test privateMethod()
        # {abstract} Test protectedMethod(Test,Pia)
        }
        class de.hdm_stuttgart.editor.data.EditorRepo {
        + void fetchMarkDown(String)
        + StringProperty getHtmlStringProperty()
        }
    """

    val mapper = UMLMapperImpl()
    mapper.mapDiagram(text)

}

