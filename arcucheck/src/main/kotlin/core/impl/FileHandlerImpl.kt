package core.impl

import core.constants.Patterns
import core.exceptions.MissingImplPathException
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path

class FileHandler {

    companion object {

        /**
         * Read all files with file extension .puml of a directory into a list
         *
         * @param directoryPath the path to the directory potentially containing .puml files
         * @return a list of .puml files
         */
        fun readDirectoryPumlFilePaths(directoryPath: String): List<File> {
            val directory = File(directoryPath)
            if (directory.isDirectory) {
                return directory.walkTopDown()
                    .filter { file -> file.extension == "puml" }
                    .toList()
            }
            throw FileNotFoundException("Specified path $directoryPath is not a directory")
        }

        /**
         * Extract the path to the related implementation of a desgin diagram from the design diagrams PlantUML file
         * The path to the implementation has to be specified as comment in the PlantUML file in the following format:
         * 'implementation_path=[path/to/impl]
         *
         * @param pumlText the complete PlantUML text resp. PlantUML file content
         * @param pumlFilePath the path where the PlantUML file is located at (only to output helpful error messages)
         * @return the extracted path to the related implementation
         */
        fun extractImplementationPath(pumlText: String, pumlFilePath: String): String {
            val pattern = Regex(Patterns.EXTRACT_IMPL_PATH)
            val match = pattern.find(pumlText)
            match?.let {
                return match.groupValues[1]
            } ?: run {
                throw MissingImplPathException(
                    "PlantUML file at $pumlFilePath does not contain the path to the related implementation. Please" +
                            " add \"\'implementation_path=[path_to_implementation]\" at the top of the PlantUML file content."
                )
            }
        }

        /**
         * Read the content of a file into a single string
         *
         * @param file the file which should be read
         * @return the whole content of the file as a string
         */
        fun readFileIntoString(file: File): String {
            try {
                return file.readText()
            } catch (exception: FileNotFoundException) {
                throw FileNotFoundException("File \"${file.name}\" at path ${file.absolutePath} does not exist.")
            }

        }


        /**
         * Create a temporary empty file
         *
         * @return the path to the temporary file
         */
        fun createTempFile(): Path {
            return kotlin.io.path.createTempFile(prefix = "codeDiagram", suffix = ".tmp")
        }
    }
}