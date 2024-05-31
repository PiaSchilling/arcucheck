package core.impl

import core.api.UMLMapper
import core.model.*

class UMLMapperImpl : UMLMapper {

    fun mapDiagram(umlText: String): PUMLDiagram {
        val umlTextClasses = splitUMLTextClasses(umlText);
        val classes = mapClasses(umlTextClasses)


        println("----")
        println(splitUMLTextClasses(umlText))
        println("-----")

        println(classes)
        return PUMLDiagram("Test")
    }

    /**
     * Spilt the complete PlantUML Text into single PlantUML Text chunks each containing one class or abstract class
     *
     * @param umlText the whole PlantUML Text as String
     * @return a List of String which each contains a single PlantUML class in text form
     */
    private fun splitUMLTextClasses(umlText: String): List<String> {
        return umlText.split(Regex("""(?=abstract\s+class|(?<!abstract\s)class)"""))
    }

    private fun mapClasses(umlTextClasses: List<String>): List<PUMLClass> {
        val mappedClasses = mutableListOf<PUMLClass>()
        umlTextClasses.forEach { textClass ->
            mappedClasses.add(mapClass(textClass))
        }
        return mappedClasses
    }

    private fun mapClass(umlText: String): PUMLClass {
        val className = mapClassName(umlText)
        val constructors = mapConstructors(umlText)
        val methods = mapMethods(umlText)
        val fields = mapFields(umlText)
        // TODO remove hardcoded false
        return PUMLClass(className,constructors,fields,methods,false)
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

        classNameMatches.forEach { matchResult ->
            pumlClassNames.add(matchResult.groupValues[1])
        }
        return pumlClassNames
    }

    private fun mapClassName(umlText: String): String {
        val classNamePattern = Regex("""class\s+([\w.]+)\s*\{""")
        val classNamePatternAbstract = Regex("""(?:abstract\s+)?class\s+([\w.]+)\s*\{""")
        val classNameMatchAbstract = classNamePatternAbstract.find(umlText)
        return classNameMatchAbstract?.groupValues?.get(1) ?: run {
            println("not abstract")
            val classNameMatch = classNamePattern.find(umlText)
             classNameMatch?.groupValues?.get(1) ?: ""
        }
// TODO hier weitermachen, um zwischen abstrakten und normalen klassen zu unterscheiden
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
        abstract class de.hdm_stuttgart.editor.data.EditorRepo {
        + void fetchMarkDown(String)
        + StringProperty getHtmlStringProperty()
        }
    """
    // TODO add interfaces

    val mapper = UMLMapperImpl()
    mapper.mapDiagram(text)

}

