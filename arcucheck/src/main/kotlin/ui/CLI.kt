package ui

import control.api.Controller
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "arcucheck", version = ["arucheck prototype v0.0.1"],
    mixinStandardHelpOptions = true,
    description = ["check your architecture"]
)
class CLI : Callable<Int>, KoinComponent {

    private val controller: Controller by inject()

    @CommandLine.Option(
        names = ["-cp", "--codePath"], paramLabel = "CODE_PATH",
        description = ["the path to the code (is state of the system)"]
    )
    private var codePath: String = ""

    @CommandLine.Option(
        names = ["-dp", "--diagramPath"], paramLabel = "DIAGRAM_PATH",
        description = ["the path to the diagram (intended state of the system)"]
    )
    private var diagramPath: String = ""
    override fun call(): Int {

        controller.onExecuteCommand(listOf(codePath, diagramPath))
        return 0
    }
}