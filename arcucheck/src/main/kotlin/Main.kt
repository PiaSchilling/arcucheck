import picocli.CommandLine
import ui.CLI


fun main(args: Array<String>) = System.exit(CommandLine(CLI()).execute(*args))
