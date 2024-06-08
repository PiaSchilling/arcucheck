package core.impl

import core.api.PUMLMapper
import core.constants.Keywords
import core.constants.Patterns
import core.model.puml.*

class PUMLMapperImpl : PUMLMapper {

    override fun mapDiagram(umlText: String): PUMLDiagram {
        val umlTextClasses = splitUMLTextClasses(umlText)
        val pumlTypes = mapTypes(umlTextClasses)
        val umlTextRelations = splitUMLTextRelations(umlText)
        val pumlRelations = mapRelations(umlTextRelations)

        val pumlClasses = pumlTypes.filterIsInstance<PUMLClass>()
        val pumlInterfaces = pumlTypes.filterIsInstance<PUMLInterface>()
        return PUMLDiagram("Test", pumlClasses, pumlInterfaces, pumlRelations) // TODO remove hardcoded name
    }

    /**
     * Spilt the complete PlantUML Text into single PlantUML Text chunks each containing one (abstract) class or interface
     *
     * @param umlText the whole PlantUML Text as String
     * @return a List of String which each contains a single PlantUML class in text form
     */
    private fun splitUMLTextClasses(umlText: String): List<String> {
        return umlText.split(Regex(Patterns.SPLIT_CLASSES))
    }

    /**
     * Spilt the complete PlantUML Text into single PlantUML Text chunks each containing one relation
     *
     * @param umlText the whole PlantUML Text as String
     * @return a List of String which each contains a single PlantUML relation in text form
     */
    private fun splitUMLTextRelations(umlText: String): List<String> {
        val relationTexts = mutableListOf<String>()
        val relationPattern = Regex(
            Patterns.SPLIT_RELATIONS,
            RegexOption.MULTILINE
        )
        relationPattern.findAll(umlText)
            .map { result -> result.groupValues[0].trim() }
            .toCollection(relationTexts)
        return relationTexts
    }

    /**
     * Map UML relations from text format to PUMLRelation models
     *
     * @param umlTextRelations a list of the text formatted relations that should be mapped
     * @return a list of mapped PUMLRelation models
     */
    private fun mapRelations(umlTextRelations: List<String>): List<PUMLRelation> {
        val mappedRelations = mutableListOf<PUMLRelation>()
        val relationPattern = Regex(Patterns.EXTRACT_RELATIONS)

        umlTextRelations.forEach { textRelation ->
            val relation = relationPattern.find(textRelation)
            relation?.let {
                val relationType = it.groupValues[2]
                val sourceClassName = it.groupValues[1]
                val destinationClassName = it.groupValues[3]

                mappedRelations.add(
                    PUMLRelation(
                        RelationType.fromString(relationType),
                        sourceClassName,
                        destinationClassName
                    )
                )
            }

        }
        return mappedRelations
    }

    /**
     * Map UML Types (classes and interfaces) from text format to PUMLType models
     *
     * @param umlTextTypes a list of the text formatted types that should be mapped
     * @return a list of mapped PUMLType models
     */
    private fun mapTypes(umlTextTypes: List<String>): List<PUMLType> {
        val mappedClasses = mutableListOf<PUMLType>()
        umlTextTypes.forEach { textClass ->
            if (textClass.isBlank()) {
                return@forEach
            } else if (textClass.contains(Keywords.INTERFACE)) {
                mappedClasses.add(mapInterface(textClass))
            } else {
                mappedClasses.add(mapClass(textClass))
            }
        }
        return mappedClasses
    }

    /**
     * Map a UML class form text format to a PUMLClass model
     *
     * @param umlText an uml class in text format that should be mapped
     * @return the mapped PUMLClass
     */
    private fun mapClass(umlText: String): PUMLClass {
        val className = mapClassName(umlText)
        val constructors = mapConstructors(umlText)
        val methods = mapMethods(umlText)
        val fields = mapFields(umlText)
        return PUMLClass(className.name, constructors, fields, methods, className.isAbstract)
    }

    /**
     * Map a UML interface form text format to a PUMLInterface model
     *
     * @param umlText an uml interface in text format that should be mapped
     * @return the mapped PUMLInterface
     */
    private fun mapInterface(umlText: String): PUMLInterface {
        val interfaceName = mapInterfaceName(umlText)
        val methods = mapMethods(umlText)
        return PUMLInterface(interfaceName, methods)
    }

    /**
     * Map all UML fields contained by a class form text format to a PUMLField models
     *
     * @param umlText an uml class in text format that contains the fields to be mapped
     * @return a list of the mapped PUMLFields
     */
    private fun mapFields(umlText: String): List<PUMLField> {
        val pumlFields = mutableListOf<PUMLField>()

        val fieldPattern = Regex(
            Patterns.EXTRACT_FIELDS,
            RegexOption.MULTILINE
        )
        val fields = fieldPattern.findAll(umlText)

        fields.forEach { field ->
            val fieldVisibility = field.groupValues[1]
            val fieldStatic = field.groupValues[2]
            val fieldDataType = field.groupValues[3]
            val fieldName = field.groupValues[4]

            val isStatic = fieldStatic.contains(Keywords.STATIC)

            pumlFields.add(
                PUMLField(fieldName, fieldDataType, Visibility.fromString(fieldVisibility), isStatic)
            )

        }
        return pumlFields
    }

    /**
     * Map all UML methods contained by a class form text format to a PUMLMethod models
     *
     * @param umlText an uml class in text format that contains the methods to be mapped
     * @return a list of the mapped PUMLMethods
     */
    private fun mapMethods(umlText: String): List<PUMLMethod> {
        val pumlMethods = mutableListOf<PUMLMethod>()

        val methodPattern = Regex(Patterns.EXTRACT_METHOD)
        val methods = methodPattern.findAll(umlText)

        methods.forEach { method ->
            val methodVisibility = method.groupValues[1]
            val methodStaticAbstract = method.groupValues[2]
            val methodReturnType = method.groupValues[4]
            val methodName = method.groupValues[5]
            val methodParameters = method.groupValues[6]

            val isAbstract = methodStaticAbstract.contains(Keywords.ABSTRACT)
            val isStatic = methodStaticAbstract.contains(Keywords.STATIC)

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

    /**
     * Map the class name of an uml class in text format
     *
     * @param umlText an uml class in text format that contains the classname to be mapped
     * @return a ClassName object which contains the name of the class and bool which indicates, if the class is abstract
     */
    private fun mapClassName(umlText: String): ClassName {
        val classNamePattern = Regex(Patterns.EXTRACT_CLASS_NAME)
        val classNameMatch = classNamePattern.find(umlText)
        val classSignature = classNameMatch?.groupValues?.get(0)
        val className = classNameMatch?.groupValues?.get(1)
        classSignature?.contains(Keywords.ABSTRACT).let {
            return ClassName(className ?: "", it ?: false)
        }
    }

    /**
     * Map the interface name of an uml interface in text format
     *
     * @param umlText an uml interface in text format that contains the interface name to be mapped
     * @return the name of the interface as a string
     */
    private fun mapInterfaceName(umlText: String): String {
        val interfaceNamePattern = Regex(Patterns.EXTRACT_INTERFACE_NAME)
        val interfaceNameMatch = interfaceNamePattern.find(umlText)
        return interfaceNameMatch?.groupValues?.get(1) ?: ""
    }

    /**
     * Map all UML constructors contained by a class form text format to PUMLConstructor models
     *
     * @param umlText an uml class in text format that contains the constructors to be mapped
     * @return a list of the mapped PUMLConstructors
     */
    private fun mapConstructors(umlText: String): List<PUMLConstructor> {
        val pumlConstructors = mutableListOf<PUMLConstructor>()

        val pattern = Regex(Patterns.EXTRACT_CONSTRUCTORS)
        val matches = pattern.findAll(umlText)

        matches.forEach { matchResult ->
            val constructorVisibility = matchResult.groupValues[1]
            val constructorParams = matchResult.groupValues[3]

            pumlConstructors.add(
                PUMLConstructor(constructorParams.split(","), Visibility.fromString(constructorVisibility))
            )
        }

        return pumlConstructors
    }
}

