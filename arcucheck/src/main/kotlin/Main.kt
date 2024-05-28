import control.di.controlModule
import core.di.coreModule
import org.koin.core.context.startKoin
import picocli.CommandLine
import ui.CLI


fun main(args: Array<String>) {
    startKoin {
        modules(coreModule, controlModule)
    }

    System.exit(CommandLine(CLI()).execute(*args))
}
