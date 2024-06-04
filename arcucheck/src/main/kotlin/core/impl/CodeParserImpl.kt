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
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.io.path.absolutePathString


class CodeParserImpl : CodeParser {

    /**
     * Parse the code of the provided path into a plantUML diagram
     * @param codePath the path to the code that should be parsed into a plantUML diagram
     */
    override fun parseCode(codePath: String): Path {

        val tempFilePath = createTempFile()

        val args = arrayOf(
            "-o",
            tempFilePath.absolutePathString(),
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
        return tempFilePath
    }

    private fun createTempFile(): Path {
        val tempFile = kotlin.io.path.createTempFile(prefix = "codeDiagram", suffix = ".tmp")
        println("Temporary file created with Kotlin extensions at: ${tempFile.toAbsolutePath()}")
        return tempFile
    }
}