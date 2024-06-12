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
import java.io.FileNotFoundException
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
            "-o", // --outfile <arg>          Set the output file
            tempFilePath,
            "-f", // --filepath <arg>         Set the input file/directory
            codePath,
            "-fdef", //--field_default       Add default fields
            "-fpri", //--field_private       Add private fields
            "-fpro", //--field_protected     Add protected fields
            "-fpub", //--field_public        Add public fields
            "-mdef", //--method_default      Add default methods
            "-mpri", //--method_private      Add private methods
            "-mpub", //--method_public       Add public methods
            "-mpro", //--method_protected    Add protected methods
            "-sctr", //--show_constructors   Show constructors
            "-spkg", //--show_package        Show package
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
        try {
            return File(path.toUri()).readText()
        } catch (exception: FileNotFoundException) {
            println("File \"${path.fileName}\" at path ${path.absolutePathString()} does not exist. Returning empty String.")
        }
        return ""
    }
}