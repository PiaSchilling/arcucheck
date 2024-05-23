package ui

import com.google.inject.Guice
import control.api.Controller
import control.di.ControlModule
import core.di.CoreModule
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name="arcucheck",version = ["arucheck prototype v0.0.1"],
    mixinStandardHelpOptions = true,
    description = ["check your architecture"])
class CLI : Callable<Int> {

    @CommandLine.Option(names = ["-cp", "--codePath"], paramLabel = "CODE_PATH",
        description = ["the path to the code (is state of the system)"])
    private var codePath: String = ""

    @CommandLine.Option(names = ["-dp", "--diagramPath"], paramLabel = "DIAGRAM_PATH",
        description = ["the path to the diagram (intended state of the system)"])
    private var diagramPath: String = ""
    override fun call(): Int {

        // TODO extract this to factory or method
        val injector = Guice.createInjector(listOf(CoreModule(), ControlModule()))
        val controller = injector.getInstance(Controller::class.java)
        controller.onExecuteCommand(listOf(codePath, diagramPath))
        return 0
    }
}