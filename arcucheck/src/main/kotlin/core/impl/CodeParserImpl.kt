package core.impl

import com.shuzijun.plantumlparser.cli.options.CliOptionsHolder
import com.shuzijun.plantumlparser.cli.options.ParserConfigUpdater
import com.shuzijun.plantumlparser.core.ParserConfig
import com.shuzijun.plantumlparser.core.ParserProgram
import core.api.CodeParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.util.function.Consumer


class CodeParserImpl : CodeParser {

    /**
     * Parse the code of the provided path into a plantUML diagram
     * @param codePath the path to the code that should be parsed into a plantUML diagram
     */
    override fun parseCode(codePath: String) {

        val args = arrayOf(
            "-o",
            "/Users/piaschilling/Desktop/output.puml", // TODO replace hardcoded output path
            "-f",
            codePath,
            "-sctr",
            "-spkg",
            "-fpub",
            "-mpub",
            "-fpro",
            "-mpro"
        )

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
}