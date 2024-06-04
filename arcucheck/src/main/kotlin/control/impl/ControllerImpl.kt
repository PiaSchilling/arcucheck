package control.impl

import control.api.Controller
import core.api.CodeParser
import core.api.PUMLMapper


class ControllerImpl(private val codeParser: CodeParser, private val PUMLMapper: PUMLMapper) : Controller {

    override fun onExecuteCommand(args: List<String>) {
        println("On exec command")
        val codeDiagram =
            codeParser.parseCode(args[0]) // TODO this is risky, add error handling (maybe create model class for programm args)
        // model class contains then codePath, diagramPath, ... and errors can be catched while creating this model

        val puml = PUMLMapper.mapDiagram(codeDiagram)
        println(puml)
    }
}