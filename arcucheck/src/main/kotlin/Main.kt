import com.shuzijun.plantumlparser.cli.options.CliOptionsHolder
import com.shuzijun.plantumlparser.cli.options.ParserConfigUpdater
import com.shuzijun.plantumlparser.core.ParserConfig
import com.shuzijun.plantumlparser.core.ParserProgram
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.util.function.Consumer


fun main(args: Array<String>) {
    try {
        val options: Options = CliOptionsHolder.createOptions()
        val parser: CommandLineParser = DefaultParser()
        val cmd: CommandLine = parser.parse(options, args)

        // Updated ParserConfig
        val config = ParserConfig()
        CliOptionsHolder.getParserConfigUpdater()
            .forEach(Consumer { parserConfigUpdater: ParserConfigUpdater ->
                parserConfigUpdater.updateConfig(
                    config,
                    cmd
                )
            })
        val program = ParserProgram(config)
        program.execute()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}
