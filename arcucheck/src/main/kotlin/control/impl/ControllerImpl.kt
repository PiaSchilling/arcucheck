package control.impl

import com.google.inject.Inject
import control.api.Controller
import core.api.CodeParser

class ControllerImpl @Inject constructor(private val codeParser: CodeParser) : Controller {

    override fun onExecuteCommand(args: List<String>) {
        codeParser.parseCode(args[0]) // TODO this is risky, add error handling (maybe create model class for programm args)
        // model class contains then codePath, diagramPath, ... and errors can be catched while creating this model

        // todo add missing core logic here
    }
}