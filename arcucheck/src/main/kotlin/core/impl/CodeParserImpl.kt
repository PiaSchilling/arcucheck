package core.impl

import com.shuzijun.plantumlparser.cli.options.CliOptionsHolder
import com.shuzijun.plantumlparser.cli.options.ParserConfigUpdater
import com.shuzijun.plantumlparser.core.ParserConfig
import com.shuzijun.plantumlparser.core.ParserProgram
import core.api.CodeParser
import core.constants.RELEASE
import core.constants.getReleaseConfig
import core.constants.getTestConfig
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
import java.io.FileNotFoundException
import java.security.AlgorithmParameterGenerator
import java.util.function.Consumer
import kotlin.io.path.absolutePathString


class CodeParserImpl : CodeParser {

    /**
     * Parse the code located at the provided path into a PlantUML diagram
     *
     * @param codePath the path to the code which should be parsed
     * @return a string containing the whole PlantUML diagram representing the code
     */
    override fun parseCode(codePath: String, config: String): String {
        if (!File(codePath).exists()) {
            throw FileNotFoundException("Code file at \"$codePath\" does not exist.")
        }
        val tempFilePath = FileHandler.createTempFile()
        val parameters: Array<String> = if (config == RELEASE) {
            getReleaseConfig(tempFilePath.absolutePathString(), codePath)
        } else {
            getTestConfig(tempFilePath.absolutePathString(), codePath)
        }
        generateDiagram(parameters)
        return FileHandler.readFileIntoString(File(tempFilePath.toUri()))
    }

    /**
     * Generate a PlantUML diagram from the code located at the provided path
     *
     * @param codePath path to the code for which the diagram should be generated
     * @param tempFilePath the location where the generated diagram will be saved (temporarily)
     */
    private fun generateDiagram(parameters: Array<String>) {

        try {
            val options: Options = CliOptionsHolder.createOptions()
            val parser: CommandLineParser = DefaultParser()
            val cmd: CommandLine = parser.parse(options, parameters)

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