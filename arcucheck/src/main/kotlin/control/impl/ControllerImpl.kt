package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.PUMLMapper
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
            val codePUMLDiagram = PUMLMapper.mapDiagram(args[0], codeDiagram,null) // TODO remove null
            val designPUMLDiagram =
                PUMLMapper.mapDiagram(args[1], designDiagram,null) // TODO implement mapping based on design diagram name

            val comparator = PUMLComparatorImpl() // TODO inject
            comparator.comparePUMLDiagrams(codePUMLDiagram, designPUMLDiagram)
        }

    }

    override fun onExecuteCommandDirectory(directoryPath: String) {
        try {
            val pumlFiles = FileHandler.readDirectoryPumlFilePaths(directoryPath)

            val textDesignDiagrams = pumlFiles.associateWith { pumlFile ->
                val textDiagram = FileHandler.readFileIntoString(pumlFile)
                textDiagram
            }.toMutableMap()

            val textImplDiagrams =
                pumlFiles.associateWith { pumlFile -> // fileName serves as path to the related code part. Kotlin replaces "/" into ":", so it has to be changed
                    // back to have a valid path
                    val textDiagram = codeParser.parseCode(pumlFile.nameWithoutExtension.replace(":", "/"))
                    textDiagram
                }.toMutableMap()


            val pumlDesignDiagrams =
                textDesignDiagrams.mapValues { textDiagram ->
                    PUMLMapper.mapDiagram(
                        sourcePath = directoryPath, // TODO fix
                        umlText = textDiagram.value,
                        diagramName = textDiagram.key.name
                    )
                }
            val pumlImplDiagrams =
                textImplDiagrams.mapValues { textDiagram ->
                    PUMLMapper.mapDiagram(
                        sourcePath = textDiagram.key.nameWithoutExtension,
                        umlText = textDiagram.value,
                        diagramName = null
                    )
                }

            val comparator = PUMLComparatorImpl() // TODO inject

            pumlDesignDiagrams.forEach { designDiagram ->
                val implDiagram = pumlImplDiagrams[designDiagram.key]
                implDiagram?.let {
                    comparator.comparePUMLDiagrams(implDiagram, designDiagram.value)
                }
            }
        } catch (e: FileNotFoundException) {
            println(e)
        }

    }
}