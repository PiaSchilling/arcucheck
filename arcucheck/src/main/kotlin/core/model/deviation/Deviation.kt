package core.model.deviation


data class Deviation(
    val level: DeviationLevel,
    val subjectType: DeviationSubjectType,
    val deviationType: DeviationType,
    val affectedClassesNames: List<String>,
    val title: String,
    val description: String,
    val affectedDesignDiagramPath: String,
    val affectedImplementationPath: String,
) {
    override fun toString(): String {
        return "\n Detected deviation \"$title\": \n Level: $level \n Subject: $subjectType \n Type: $deviationType \n Cause: $description \n " +
                "Affected design diagram: $affectedDesignDiagramPath \n Affected implementation at: $affectedImplementationPath \n"
    }
}
