package core.impl

import core.api.CodeParser


class CodeParserImpl : CodeParser {

    /**
     * Parse the code of the provided path into a plantUML diagram
     * @param args the arguments for the parsing (path to codebase, path to output dir, ...)
     */
    override fun parseCode(args: Array<String>) {
        //TODO comment in when .jar files are available

        /* try {
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
         }*/
    }
}