package core.model

enum class Visibility(val pumlCharacter:String) {
    PUBLIC("+"),
    PRIVATE("-"),
    PROTECTED("#"),
    PACKAGE_PRIVATE("~");

    companion object {
        /**
         * Returns the matching Visibility constant for the provided string
         *
         * @param string string representation of a visibility (+,-,#,~)
         * @return
         */
        fun fromString(string: String): Visibility {
            return Visibility.values().first { visibility -> visibility.pumlCharacter == string }
        }
    }
}
