package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.UMLMapper


class ControllerImpl(private val codeParser: CodeParser, private val umlMapper: UMLMapper) : Controller {

    override fun onExecuteCommand(args: List<String>) {
        println("On exec command")
        val codeDiagram =
            codeParser.parseCode(args[0]) // TODO this is risky, add error handling (maybe create model class for programm args)
        // model class contains then codePath, diagramPath, ... and errors can be catched while creating this model

        val puml = umlMapper.mapDiagram(codeDiagram)
        println(puml)
    }
}