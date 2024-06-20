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
            val codePUMLDiagram = PUMLMapper.mapDiagram(codeDiagram)
            val designPUMLDiagram = PUMLMapper.mapDiagram(designDiagram)

            val comparator = PUMLComparatorImpl() // TODO inject
            comparator.comparePUMLDiagrams(codePUMLDiagram, designPUMLDiagram)
        }

    }

    override fun onExecuteCommandDirectory(directoryPath: String) {
        try {
            val pumlFiles = FileHandler.readDirectoryPumlFilePaths(directoryPath)

            val textDesignDiagrams = pumlFiles.associate { pumlFile ->
                val fileName = pumlFile.nameWithoutExtension
                val textDiagram = FileHandler.readFileIntoString(pumlFile)
                fileName to textDiagram
            }.toMutableMap()

            val textImplDiagrams = pumlFiles.associate { pumlFile ->
                val fileName = pumlFile.nameWithoutExtension
                // fileName serves as path to the related code part. Kotlin replaces "/" into ":", so it has to be changed
                // back to have a valid path
                val textDiagram = codeParser.parseCode(fileName.replace(":","/"))
                fileName to textDiagram
            }.toMutableMap()

            val pumlDesignDiagrams =
                textDesignDiagrams.mapValues { textDiagram -> PUMLMapper.mapDiagram(textDiagram.value) }
            val pumlImplDiagrams =
                textImplDiagrams.mapValues { textDiagram -> PUMLMapper.mapDiagram(textDiagram.value) }

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