package core.model.deviation


data class Deviation(
    val level: DeviationLevel,
    val area: DeviationArea,
    val type: DeviationType,
    val affectedClassesNames: List<String>,
    val title: String,
    val description: String,
) {
    override fun toString(): String {
        return "Detected deviation \"$title\": \n Level: $level \n Area: $area \n Type: $type \n Cause: $description \n"
    }
}
