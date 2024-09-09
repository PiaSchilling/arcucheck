package core.constants

/**
 * Contains constant strings representing keywords
 */
object Keywords {
    const val INTERFACE = "interface"
    const val STATIC = "static"
    const val ABSTRACT = "abstract"
}

/**
 * Contains constant strings representing regex patterns
 */
object Patterns {

    /**
     * Pattern to split PlantUML diagram into PlantUML classes
     */
    const val SPLIT_CLASSES = """(?=abstract\s+class|(?<!abstract\s)class|interface)"""

    /**
     * Pattern to split PlantUML diagram into PlantUML relations
     * - group 0: whole relation string
     */
    const val SPLIT_RELATIONS = """^\s*[\w.]+\s*[<>x\-o|*.]{2,4}\s*[\w.]+\s*${'$'}"""

    /**
     * Pattern to extract relation character groups from PlantUML relation string
     * - group 0: whole relation string
     * - group 1: sourceClass of relation
     * - group 2: relation type
     * - group 3: destination class of relation
     */
    const val EXTRACT_RELATIONS = """^\s*([\w.]+)\s*([<>x\-o|*.]{2,4})\s*([\w.]+)\s*${'$'}"""

    /**
     * Pattern to extract field character groups from PlantUML class string
     * - group 0: whole field signature
     * - group 1: field visibility
     * - group 2: isStatic or not
     * - group 3: field data type
     * - group 4: field name
     */
    const val EXTRACT_FIELDS = """([+\-#~])\s*(\{(?:static)?})?\s*(\w+)\s+(\w+)(?!${'$'}\()(?!\()${'$'}"""

    /**
     * Pattern to extract method character groups from PlantUML class string
     * - group 0: whole method signature
     * - group 1: method visibility
     * - group 2: isStatic or isAbstract or not
     * - group 4: method return type
     * - group 5: method name
     * - group 6: method parameters
     */
    const val EXTRACT_METHOD = """([+\-#~])?\s*(\{(?:static|abstract)?})?\s*((\w+(?:<[\w<>]+>)?)\s+(\w+)\((.*?)\))"""

    /**
     * Pattern to extract the class name with optional "abstract" modifier from a PlantUML class string
     */
    const val EXTRACT_CLASS_NAME = """(?:abstract\s+)?class\s+([\w.]+)\s*\{"""

    /**
     * Pattern to extract the interface name from a PlantUML class string
     */
    const val EXTRACT_INTERFACE_NAME = """interface\s+([\w.]+)\s*\{"""


    /**
     * Pattern to extract constructors character groups from PlantUML class string
     * - group 0: whole constructors signature
     * - group 1: constructors visibility
     * - group 2: constructors name
     * - group 3: constructors parameters
     */
    const val EXTRACT_CONSTRUCTORS = """([+\-#~])\s*<<Create>>\s+(\w+)\((.*?)\)"""

    const val EXTRACT_IMPL_PATH = """'implementation_path=\[(.+)\]"""
}
