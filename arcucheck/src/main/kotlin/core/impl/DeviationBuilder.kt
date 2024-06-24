package core.impl

import core.model.deviation.*
import util.extensions.concatOverlap

class DeviationBuilder {

    companion object {

        /**
         * Build the description for deviations that are caused by absent or unexpected subjects
         *
         * @param deviationType either UNEXPECTED or ABSENT
         * @param subjectType the affected subjects type (e.g. field, class, method,...)
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @return a string containing the assembled deviation description
         */
        private fun buildUnexpectedAbsentDescription(
            deviationType: DeviationType,
            subjectType: DeviationSubjectType,
            subjectName: String,
            classLocation: String,
            affectedClassName: String,
            ): String {
            var location = classLocation
            if(subjectType == DeviationSubjectType.FIELD || subjectType == DeviationSubjectType.METHOD){
                location = "$classLocation.$affectedClassName"
            }
            val unexpectedDeviationLocation =
                "${subjectType.asString} \"${subjectName}\" found in \"${location}\""
            val absentDeviationLocation =
                "${subjectType.asString} \"${subjectName}\" is expected in \"${location}\""
            return when (deviationType) {
                DeviationType.UNEXPECTED -> " $unexpectedDeviationLocation is present in the implementation, but not expected according to the design."
                DeviationType.ABSENCE -> " $absentDeviationLocation according to the design, but not found in the implementation."
                else -> ""
            }
        }

        /**
         * Build deviations that are caused by absent or unexpected subjects
         *
         * @param level deviation level (Makro for architecture level, mikro for code level)
         * @param subjectType the affected subjects type
         * @param deviationType either unexpected or absent
         * @param affectedClassName the names of the affected classes
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @return the fully assembled deviation object encapsulating all the information
         */
        fun buildUnexpectedAbsentDeviation(
            level: DeviationLevel,
            subjectType: DeviationSubjectType,
            deviationType: DeviationType,
            affectedClassName: String,
            subjectName: String,
            classLocation: String,
            designDiagramPath: String,
            implPath: String,
        ): Deviation {
            return Deviation(
                level,
                subjectType,
                deviationType,
                listOf(affectedClassName),
                "${deviationType.asAdjective} ${subjectType.name.lowercase()}",
                buildUnexpectedAbsentDescription(
                    deviationType,
                    subjectType,
                    subjectName,
                    classLocation,
                    affectedClassName
                ),
                designDiagramPath,
                implPath.concatOverlap("${classLocation.replace(".", "/")}/$affectedClassName.java")
            )
        }


        /**
         * Build the description for deviations that are caused by a wrong implementation (e.g. wrong parameter types of a method,...)
         *
         *
         * @param subjectType the affected subjects type (e.g. field, class, method,...)
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @param causes a list of cause-descriptions which explain what causes the deviation (e.g. wrong visibility)
         * @return a string containing the assembled deviation description
         */
        private fun buildMisimplementedDescription(
            subjectType: DeviationSubjectType,
            subjectName: String,
            classLocation: String,
            causes: List<String>,
            affectedClassName: String,
        ): String {
            return "Implementation of ${subjectType.name.lowercase()} \"$subjectName\" located in \"$classLocation.$affectedClassName\" " + // TODO maybe add "locatd in interface/class
                    "deviates from the design: $causes"
        }

        /**
         * Build deviations that are caused by a wrong implementation (e.g. wrong parameter types of a method,...)
         *
         * @param level deviation level (Makro for architecture level, mikro for code level)
         * @param subjectType the affected subjects type
         * @param affectedClassName the names of the affected classes
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @param causes a list of cause-descriptions which explain what causes the deviation (e.g. wrong visibility)
         * @return the fully assembled deviation object encapsulating all the information
         */
        fun buildMisimplementedDeviation(
            level: DeviationLevel,
            subjectType: DeviationSubjectType,
            affectedClassName: String,
            subjectName: String,
            classLocation: String,
            causes: List<String>,
            designDiagramPath: String,
            implPath: String,
        ): Deviation {
            val type = DeviationType.MISIMPLEMENTED
            return Deviation(
                level,
                subjectType,
                type,
                listOf(affectedClassName),
                "Deviating ${subjectType.name.lowercase()} implementation",
                buildMisimplementedDescription(subjectType, subjectName, classLocation, causes, affectedClassName),
                designDiagramPath,
                implPath.concatOverlap("${classLocation.replace(".", "/")}/$affectedClassName.java")
            )

        }

    }


}