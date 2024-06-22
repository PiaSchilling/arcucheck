package util.extensions

/**
 * Concatenates two strings. If the strings contain an overlapping sequence, the overlapping sequence
 * will only be added once. Example: a="ABCD", b="CDE" -> result="ABCDE" (not ABCDCDE).
 *
 * @param other the string to be concatenated to this string
 * @return the concatenated string without a repeated overlapping sequence
 */
fun String.concatOverlap(other: String): String {
    for (i in 0..length) {
        val temp = this.substring(i, length)
        for (j in 0..other.length) {
            val otherTemp = other.substring(0, other.length - j)
            if (otherTemp == temp) {
                val begin = this.removeSuffix(otherTemp)
                val end = other.removePrefix(otherTemp)
                return begin + otherTemp + end
            }
        }
    }

    return this + other
}