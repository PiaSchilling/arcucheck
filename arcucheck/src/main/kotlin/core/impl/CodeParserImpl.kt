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
import java.io.File
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.io.path.absolutePathString


class CodeParserImpl : CodeParser {

    override fun parseCode(codePath: String): String {
        val tempFilePath = createTempFile()
        generateDiagram(codePath, tempFilePath.absolutePathString())
        return readFileIntoString(tempFilePath)
    }

    /**
     * Generate a PlantUML diagram from the code located at the provided path
     *
     * @param codePath path to the code for which the diagram should be generated
     * @param tempFilePath the location where the generated diagram will be saved (temporarily)
     */
    private fun generateDiagram(codePath: String, tempFilePath: String) {
        val args = arrayOf(
            "-o",
            tempFilePath,
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

    /**
     * Create a temporary empty file
     *
     * @return the path to the temporary file
     */
    private fun createTempFile(): Path {
        return kotlin.io.path.createTempFile(prefix = "codeDiagram", suffix = ".tmp")
    }

    /**
     * Read the content of a file into a single string
     *
     * @param path path to the file which should be read
     * @return the whole content of the file as a string
     */ // TODO extract to separate class
    override fun readFileIntoString(path: Path): String {
        return File(path.toUri()).readText()
    }
}