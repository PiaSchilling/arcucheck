package core.model.PUML

enum class Visibility(val pumlIdentifier:String) {
    PUBLIC("+"),
    PRIVATE("-"),
    PROTECTED("#"),
    PACKAGE_PRIVATE("~");

    companion object {
        /**
         * Returns the matching Visibility enum constant for the provided string identifier
         *
         * @param string string representation of a visibility (+,-,#,~)
         * @return the matching Visibility enum constant
         */
        fun fromString(string: String): Visibility {
            return Visibility.values().first { visibility -> visibility.pumlIdentifier == string }
        }
    }
}
