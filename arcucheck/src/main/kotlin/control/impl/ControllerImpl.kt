package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.PUMLMapper
import core.exceptions.MissingImplPathException
import core.impl.FileHandler
import core.impl.PUMLComparatorImpl
import java.io.File
import java.io.FileNotFoundException


class ControllerImpl(private val codeParser: CodeParser, private val PUMLMapper: PUMLMapper) : Controller {

    override fun onExecuteCommandSingleFile(args: List<String>) {
        println("On exec command")
        val codeDiagram =
            codeParser.parseCode(args[0]) // TODO this is risky, add error handling (maybe create model class for programm args) model class contains then codePath, diagramPath, ... and errors can be catched while creating this model
        println("code diagram - - - - - - - - - - - - - ")
        println(codeDiagram)

        val designDiagram = FileHandler.readFileIntoString(File(args[1]))
        println("design diagram - - - - - - - - - - - - - -")
        println(designDiagram)
        println("- - - - - - - - - - - - - -- - - - - - -  ")

        if (codeDiagram.isNotBlank() && designDiagram.isNotBlank()) {
            val codePUMLDiagram = PUMLMapper.mapDiagram(args[0], codeDiagram) // TODO remove null
            val designPUMLDiagram =
                PUMLMapper.mapDiagram(args[1], designDiagram) // TODO implement mapping based on design diagram name

            val comparator = PUMLComparatorImpl() // TODO inject
            comparator.comparePUMLDiagrams(codePUMLDiagram, designPUMLDiagram)
        }

    }

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
                    val value = PUMLMapper.mapDiagram(
                        sourcePath = entry.key.path,
                        umlText = entry.value,
                    )
                    val key = entry.key
                    key to value
                }

            val pumlImplDiagrams =
                textImplDiagrams.mapValues { textDiagram ->
                    PUMLMapper.mapDiagram(
                        sourcePath = textDiagram.value.second,
                        umlText = textDiagram.value.first,
                    )
                }

            val comparator = PUMLComparatorImpl() // TODO inject

            pumlDesignDiagrams.forEach { designDiagram ->
                val implDiagram = pumlImplDiagrams[designDiagram.key]
                implDiagram?.let {
                    comparator.comparePUMLDiagrams(implDiagram, designDiagram.value)
                }
            }
        } catch (fileNotFound: FileNotFoundException) {
            println(fileNotFound)
        } catch (missingCodePath: MissingImplPathException) {
            println(missingCodePath)
        }

    }
}