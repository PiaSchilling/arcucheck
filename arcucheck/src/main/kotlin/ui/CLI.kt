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
        names = ["-d", "--directory"], paramLabel = "DIRECTORY",
        description = ["Use a directory containing puml files as input."]
    )
    private var isDirectory = false

    @CommandLine.Option(
        names = ["-f", "--file"], paramLabel = "FILE",
        description = ["Use a single file as input."]
    )
    private var isFile = false

    @CommandLine.Option(
        names = ["-cp", "--codePath"], paramLabel = "CODE_PATH",
        description = ["the path to the code (is state of the system)"]
    )
    private var codePath: String = ""

    @CommandLine.Option(
        names = ["-dfp", "--diagramFilePath"], paramLabel = "DIAGRAM_FILE_PATH",
        description = ["the path to a single plant uml diagram (intended state of the system)"]
    )
    private var diagramFilePath: String = ""

    @CommandLine.Option(
        names = ["-ddp", "--diagramDirectoryPath"], paramLabel = "DIAGRAM_DIRECTORY_PATH",
        description = ["the path to a directory of plant uml diagrams (intended state of the system)"]
    )
    private var diagramDirectoryPath: String = ""
    override fun call(): Int {
        if (isFile) {
            if (diagramFilePath.isEmpty()) {
                println("You have to specify the path to the design diagram (-dfp)")
                return 1
            }
            if (codePath.isEmpty()) {
                println("You have to specify the path to the code (-cp)")
                return 1
            }
            controller.onExecuteCommandSingleFile(codePath, diagramFilePath)
        } else if (isDirectory) {
            if (diagramDirectoryPath.isEmpty()) {
                println("You have to specify the path to the directory containing the design diagrams (-ddp)")
                return 1
            }
            controller.onExecuteCommandDirectory(diagramDirectoryPath)
        } else {
            println("You have to specify one of the following options: -f or -d. Type --help for usage information.")
        }

        return 0
    }
}