package core.api

import java.nio.file.Path

interface CodeParser {
    fun parseCode(codePath:String) : Path
}