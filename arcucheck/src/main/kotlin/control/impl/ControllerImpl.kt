package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.PUMLComparator
import core.api.PUMLMapper
import core.exceptions.MissingImplPathException
import core.impl.FileHandler
import core.model.deviation.Deviation
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationSubjectType
import core.model.deviation.DeviationType
import core.model.puml.PUMLDiagram
import java.io.File
import java.io.FileNotFoundException


class ControllerImpl(
    private val codeParser: CodeParser,
    private val pumlMapper: PUMLMapper,
    private val pumlComparator: PUMLComparator
) : Controller {

    override fun onExecuteCommandSingleFile(implementationPath: String, designPath: String) {
        val codeDiagram = codeParser.parseCode(implementationPath)
        val designDiagram = FileHandler.readFileIntoString(File(designPath))

        if (codeDiagram.isNotBlank() && designDiagram.isNotBlank()) {
            val codePUMLDiagram = pumlMapper.mapDiagram(implementationPath, codeDiagram)
            val designPUMLDiagram =
                pumlMapper.mapDiagram(designPath, designDiagram)

            val deviations = pumlComparator.comparePUMLDiagrams(codePUMLDiagram, designPUMLDiagram)
            handleResult(1, deviations)
        }

    }


    override fun onExecuteCommandDirectory(directoryPath: String) {
        try {
            val deviations = mutableListOf<Deviation>()

            val designFiles = FileHandler.readDirectoryPumlFilePaths(directoryPath)
            val textDesignDiagrams = convertDesignFilesInPumlString(designFiles)
            val implementationFiles = extractImplementationFiles(textDesignDiagrams)
            val textImplDiagrams = convertImplFilesInPumlString(implementationFiles)
            val pumlDesignDiagrams = mapDesignDiagram(textDesignDiagrams)
            val pumlImplDiagrams = mapImplDiagrams(textImplDiagrams)

            pumlDesignDiagrams.forEach { designDiagram ->
                val implDiagram = pumlImplDiagrams[designDiagram.key]
                implDiagram?.let {
                    deviations.addAll(pumlComparator.comparePUMLDiagrams(implDiagram, designDiagram.value))
                }
            }

            handleResult(designFiles.size, deviations)
        } catch (fileNotFound: FileNotFoundException) {
            println(fileNotFound)
        } catch (missingCodePath: MissingImplPathException) {
            println(missingCodePath)
        }

    }

    /**
     * Handle the comparison result
     *
     * @param deviations the detected deviations of the comparison between design and implementation
     */
    private fun handleResult(fileCount: Int, deviations: List<Deviation>) {
        println("===== Deviation analysis result ===== \n")
        println("Compared PlantUML design files to its related implementation: $fileCount\n")
        if (deviations.isEmpty()) {
            println("No deviations between design and implementation found.")
        } else {
            printStatistics(deviations)
        }
    }

    private fun printStatistics(deviations: List<Deviation>) {
        val makroDeviationCount = deviations.count { deviation -> deviation.level == DeviationLevel.MAKRO }
        val mikroDeviationCount = deviations.count { deviation -> deviation.level == DeviationLevel.MIKRO }

        val absentDeviationCount = deviations.count { deviation -> deviation.deviationType == DeviationType.ABSENT }
        val unexpectedDeviationCount =
            deviations.count { deviation -> deviation.deviationType == DeviationType.UNEXPECTED }
        val misimplementedDeviationCount =
            deviations.count { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED }

        val relationDeviationCount =
            deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.RELATION }
        val packageDeviationCount =
            deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.PACKAGE }
        val classDeviationCount =
            deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.CLASS }
        val interfaceDeviationCount =
            deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.INTERFACE }
        val methodDeviationCount =
            deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.METHOD }
        val constructorDeviationCount =
            deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.CONSTRUCTOR }
        val fieldDeviationCount =
            deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.FIELD }


        println("Total deviations found: ${deviations.size}")
        println()

        println("----------------------------------------------------------------------------------")
        println("Breakdown by deviation LEVEL")
        println("Level   Description                                                 Count")
        println("..................................................................................")
        println("MAKRO   Architectural level, impacting overall structure and design $makroDeviationCount deviations")
        println("MIKRO   Code level, affecting specific implementations and details  $mikroDeviationCount deviations")
        println("----------------------------------------------------------------------------------")
        println()

        println("--------------------------------------------------------------------------------------------------------------------")
        println("Breakdown by deviation TYPE")
        println("Type             Description                                                                            Count")
        println("....................................................................................................................")
        println("ABSENT           Subject expected in the design but missing in the implementation                       $absentDeviationCount deviations")
        println("UNEXPECTED       Subject not expected in the design but present in the implementation                   $unexpectedDeviationCount deviations")
        println("MISIMPLEMENTED   Subject expected in the design, present in the implementation but wrongly implemented  $misimplementedDeviationCount deviations")
        println("--------------------------------------------------------------------------------------------------------------------")
        println()

        println("--------------------------------------------------------------")
        println("Breakdown by deviation SUBJECT TYPE")
        println("Subject type   Description                        Count")
        println("..............................................................")
        println("RELATION       Deviations affecting relations     $relationDeviationCount deviations")
        println("PACKAGE        Deviations affecting packages      $packageDeviationCount deviations")
        println("CLASS          Deviations affecting classes       $classDeviationCount deviations")
        println("INTERFACE      Deviations affecting interfaces    $interfaceDeviationCount deviations")
        println("METHOD         Deviations affecting methods       $methodDeviationCount deviations")
        println("CONSTRUCTOR    Deviations affecting constructors  $constructorDeviationCount deviations")
        println("FIELD          Deviations affecting fields        $fieldDeviationCount deviations")
        println("--------------------------------------------------------------")
        println()

        println("List of detected deviations:")
        println(deviations.joinToString("\n"))
    }

    /**
     * Extract the PlantUML code from each provided .puml-file into a string
     *
     * @param designFiles .puml-files which should be read into a string each
     * @return a map containing the PlantUML String as value and the input file as key
     */
    private fun convertDesignFilesInPumlString(designFiles: List<File>): Map<File, String> {
        return designFiles.associateWith { designFile ->
            val textDiagram = FileHandler.readFileIntoString(designFile)
            textDiagram
        }.toMutableMap()
    }

    /**
     * Extract the implementation files from the provided design diagrams (the design diagrams have to contain the
     * 'implementation_path=[] identifier, so the path to the implementation can be extracted)
     *
     * @param textDesignDiagrams PlantUML diagrams (PlantUML code in a String)
     * @return a map containing the input file as key and the path to the implementation as value
     */
    private fun extractImplementationFiles(textDesignDiagrams: Map<File, String>): Map<File, String> {
        return textDesignDiagrams.entries.associate { diagram ->
            val implPath = FileHandler.extractImplementationPath(
                diagram.value,
                diagram.key.path
            )
            val key = diagram.key
            key to implPath
        }
    }

    /**
     * Parse each provided implementation file into a PlantUML diagram
     *
     * @param implementationFiles a map where the key is the related design file and the value is the path to the implementation
     * @return a map containing the input file as key, and a Pair as value. The first value of the pair is the created
     * PlantUML diagram in a string, the second value is the original path to the implementation)
     */
    private fun convertImplFilesInPumlString(implementationFiles: Map<File, String>): Map<File, Pair<String, String>> {
        return implementationFiles.entries.associate { implFile ->
            val textDiagram = codeParser.parseCode(implFile.value)
            implFile.key to Pair(textDiagram, implFile.value)
        }.toMutableMap()
    }

    /**
     * Convert the string representation of a PlantUML design diagram into an object representation (PUMLDiagram)
     *
     * @param textDesignDiagrams a map where the key is the design file and the value is the text representation of the
     * diagram
     * @return a map where the key is the design file (same as input key) and the value is the mapped object
     * representation of the diagram
     */
    private fun mapDesignDiagram(textDesignDiagrams: Map<File, String>): Map<File, PUMLDiagram> {
        return textDesignDiagrams.entries.associate { entry ->
            val mappedDiagram = pumlMapper.mapDiagram(
                sourcePath = entry.key.path,
                umlText = entry.value,
            )
            val key = entry.key
            key to mappedDiagram
        }
    }

    /**
     * Convert the string representation of a PlantUML impl diagram into an object representation (PUMLDiagram)
     *
     * @param textImplDiagrams a map where the key is the design file and the value is a Pair. First value of the Pair
     * is the string representation of the puml diagram. Second value of the pair is the path to the implementation.
     * diagram
     * @return a map where the key is the design file (same as input key) and the value is the mapped object
     * representation of the diagram
     */
    private fun mapImplDiagrams(textImplDiagrams: Map<File, Pair<String, String>>): Map<File, PUMLDiagram> {
        return textImplDiagrams.mapValues { textDiagram ->
            pumlMapper.mapDiagram(
                sourcePath = textDiagram.value.second,
                umlText = textDiagram.value.first,
            )
        }
    }
}