package core.impl

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path

class FileHandler {

    companion object {

        /**
         * Correct the provided path into a valid path format by replacing all | into /
         *
         * @param pathToCorrect a path which contains | instead of the required / as path separators
         * @return the corrected path
         */
        private fun correctPathSeparators(pathToCorrect: String): String {
            return pathToCorrect.replace(",", "/")
        }

        /**
         * Read all files with file extension .puml of a directory into a list
         *
         * @param directoryPath the path to the directory potentially containing .puml files
         * @return a list of .puml files
         */
        fun readDirectoryPumlFilePaths(directoryPath: String): List<File> {
            val correctedPath = correctPathSeparators(directoryPath)
            val directory = File(correctedPath)
            if (directory.isDirectory) {
                return directory.walkTopDown()
                    .filter { file -> file.extension == "puml" }
                    .toList()
            }
            throw FileNotFoundException("Specified path $directoryPath is not a directory")
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
                println("File \"${file.name}\" at path ${file.absolutePath} does not exist. Returning empty String.")
            }
            return ""
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