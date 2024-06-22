package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.PUMLComparator
import core.api.PUMLMapper
import core.exceptions.MissingImplPathException
import core.impl.FileHandler
import core.model.deviation.Deviation
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
            handleResult(deviations)
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

            handleResult(deviations)
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
    private fun handleResult(deviations: List<Deviation>) {
        if (deviations.isEmpty()) {
            println("No deviations between design and implementation found") // TODO maybe revise
        } else {
            println("${deviations.size} deviation(s) between design and implementation detected:") // todo maybe add stats e.g how many makro, mikro etc.
            println(deviations)
        }
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