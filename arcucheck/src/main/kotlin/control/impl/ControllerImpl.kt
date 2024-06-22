package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.PUMLComparator
import core.api.PUMLMapper
import core.exceptions.MissingImplPathException
import core.impl.FileHandler
import core.impl.PUMLComparatorImpl
import core.model.deviation.Deviation
import java.io.File
import java.io.FileNotFoundException


class ControllerImpl(
    private val codeParser: CodeParser,
    private val pumlMapper: PUMLMapper,
    private val pumlComparator: PUMLComparator
) : Controller {

    override fun onExecuteCommandSingleFile(args: List<String>) {
        val codeDiagram = codeParser.parseCode(args[0])
        val designDiagram = FileHandler.readFileIntoString(File(args[1]))

        if (codeDiagram.isNotBlank() && designDiagram.isNotBlank()) {
            val codePUMLDiagram = pumlMapper.mapDiagram(args[0], codeDiagram)
            val designPUMLDiagram =
                pumlMapper.mapDiagram(args[1], designDiagram)

            val deviations = pumlComparator.comparePUMLDiagrams(codePUMLDiagram, designPUMLDiagram)

            if (deviations.isEmpty()) {
                println("No deviations between design and implementation found")
            } else {
                println(deviations)
            }
        }

    }

    // TODO split up methods
    override fun onExecuteCommandDirectory(directoryPath: String) {
        try {
            val designFiles = FileHandler.readDirectoryPumlFilePaths(directoryPath)

            val textDesignDiagrams = designFiles.associateWith { designFile ->
                val textDiagram = FileHandler.readFileIntoString(designFile)
                textDiagram
            }.toMutableMap()

            val implFiles = textDesignDiagrams.entries.associate { diagram ->
                val value = FileHandler.extractImplementationPath(
                    diagram.value,
                    diagram.key.path
                )
                val key = diagram.key
                key to value
            }

            val textImplDiagrams =
                implFiles.entries.associate { implFile ->
                    val textDiagram = codeParser.parseCode(implFile.value)
                    implFile.key to Pair(textDiagram, implFile.value)
                }.toMutableMap()


            val pumlDesignDiagrams =
                textDesignDiagrams.entries.associate { entry ->
                    val value = pumlMapper.mapDiagram(
                        sourcePath = entry.key.path,
                        umlText = entry.value,
                    )
                    val key = entry.key
                    key to value
                }

            val pumlImplDiagrams =
                textImplDiagrams.mapValues { textDiagram ->
                    pumlMapper.mapDiagram(
                        sourcePath = textDiagram.value.second,
                        umlText = textDiagram.value.first,
                    )
                }

            val deviations = mutableListOf<Deviation>()

            pumlDesignDiagrams.forEach { designDiagram ->
                val implDiagram = pumlImplDiagrams[designDiagram.key]
                implDiagram?.let {
                    deviations.addAll(pumlComparator.comparePUMLDiagrams(implDiagram, designDiagram.value))
                }
            }
            if (deviations.isEmpty()) {
                println("No deviations between design and implementation found") // TODO maybe revise
            } else {
                println("${deviations.size} deviation(s) between design and implementation detected:") // todo maybe add stats e.g how many makro, mikro etc.
                println(deviations)
            }
        } catch (fileNotFound: FileNotFoundException) {
            println(fileNotFound)
        } catch (missingCodePath: MissingImplPathException) {
            println(missingCodePath)
        }

    }
}