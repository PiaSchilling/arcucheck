package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.PUMLMapper
import core.impl.PUMLComparatorImpl
import kotlin.io.path.Path


class ControllerImpl(private val codeParser: CodeParser, private val PUMLMapper: PUMLMapper) : Controller {

    override fun onExecuteCommand(args: List<String>) {
        println("On exec command")
        val codeDiagram =
            codeParser.parseCode(args[0]) // TODO this is risky, add error handling (maybe create model class for programm args) model class contains then codePath, diagramPath, ... and errors can be catched while creating this model
        println("code diagram - - - - - - - - - - - - - ")
        println(codeDiagram)

        val designDiagram = codeParser.readFileIntoString(Path(args[1]))
        println("design diagram - - - - - - - - - - - - - -")
        println(designDiagram)

        println("- - - - - - - - - - - - - -- - - - - - - - - - - - - - - - - - ")
        val codePUMLDiagram = PUMLMapper.mapDiagram(codeDiagram)
        println("codePUMLDiagram: $codePUMLDiagram")
        val designPUMLDiagram = PUMLMapper.mapDiagram(designDiagram)
        println("designDiagram $designPUMLDiagram")

        val comparator = PUMLComparatorImpl()
        comparator.comparePUMLDiagrams(codePUMLDiagram,designPUMLDiagram)
    }
}