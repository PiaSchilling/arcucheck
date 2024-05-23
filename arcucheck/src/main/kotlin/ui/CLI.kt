package ui

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
        println("test")
        println(codePath)
        println(diagramPath)
        return 0
    }
}

fun main(args: Array<String>) = System.exit(CommandLine(CLI()).execute(*args))