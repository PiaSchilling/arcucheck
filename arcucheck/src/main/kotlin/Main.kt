import control.di.controlModule
import core.di.coreModule
import org.koin.core.context.startKoin
import picocli.CommandLine
import ui.CLI
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    startKoin {
        modules(coreModule, controlModule)
    }

    exitProcess(CommandLine(CLI()).execute(*args))
}
