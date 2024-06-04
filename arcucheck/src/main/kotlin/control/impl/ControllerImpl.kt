package control.impl

import control.api.Controller
import core.api.CodeParser
import core.impl.UMLMapperImpl
import java.io.File

class ControllerImpl(private val codeParser: CodeParser) : Controller {

    override fun onExecuteCommand(args: List<String>) {
        println("On exec command")
        val path =
            codeParser.parseCode(args[0]) // TODO this is risky, add error handling (maybe create model class for programm args)
        // model class contains then codePath, diagramPath, ... and errors can be catched while creating this model

        // todo add missing core logic
        val mapper = UMLMapperImpl()
        val codeDiagram = File(path.toUri()).readText()
        println(codeDiagram)
        val puml = mapper.mapDiagram(codeDiagram)
        println(puml)
    }
}