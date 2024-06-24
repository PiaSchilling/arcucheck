package core.impl

import core.model.deviation.*
import util.extensions.concatOverlap

class DeviationBuilder {

    companion object {

        /**
         * Build the description for deviations that are caused by absent or unexpected subjects
         *
         * @param deviationType either UNEXPECTED or ABSENT
         * @param subject the affected subject (e.g. field, class, method,...)
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @return a string containing the assembled deviation description
         */
        private fun buildUnexpectedAbsentDescription(
            deviationType: DeviationType,
            subject: DeviationSubject,
            subjectName: String,
            classLocation: String,
            affectedClassName: String,
            ): String {
            var location = classLocation
            if(subject == DeviationSubject.FIELD || subject == DeviationSubject.METHOD){
                location = "$classLocation.$affectedClassName"
            }
            val unexpectedDeviationLocation =
                "${subject.asString} \"${subjectName}\" found in \"${location}\""
            val absentDeviationLocation =
                "${subject.asString} \"${subjectName}\" is expected in \"${location}\""
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
         * @param area the area affected
         * @param type either unexpected or absent
         * @param affectedClassName the names of the affected classes
         * @param subject the affected subject (e.g. field, class, method,...)
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @return the fully assembled deviation object encapsulating all the information
         */
        fun buildUnexpectedAbsentDeviation(
            level: DeviationLevel,
            area: DeviationArea,
            type: DeviationType,
            affectedClassName: String,
            subject: DeviationSubject,
            subjectName: String,
            classLocation: String,
            designDiagramPath: String,
            implPath: String,
        ): Deviation {
            return Deviation(
                level,
                area,
                type,
                listOf(affectedClassName),
                "${type.asAdjective} ${subject.name.lowercase()}",
                buildUnexpectedAbsentDescription(
                    type,
                    subject,
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
         * @param subject the affected subject (e.g. field, class, method,...)
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @param causes a list of cause-descriptions which explain what causes the deviation (e.g. wrong visibility)
         * @return a string containing the assembled deviation description
         */
        private fun buildMisimplementedDescription(
            subject: DeviationSubject,
            subjectName: String,
            classLocation: String,
            causes: List<String>,
            affectedClassName: String,
        ): String {
            return "Implementation of ${subject.name.lowercase()} \"$subjectName\" located in \"$classLocation.$affectedClassName\" " + // TODO maybe add "locatd in interface/class
                    "deviates from the design: $causes"
        }

        /**
         * Build deviations that are caused by a wrong implementation (e.g. wrong parameter types of a method,...)
         *
         * @param level deviation level (Makro for architecture level, mikro for code level)
         * @param area the area affected (todo fix)
         * @param affectedClassName the names of the affected classes
         * @param subject the affected subject (e.g. field, class, method,...)
         * @param subjectName the name of the affected subject (e.g. class name, method name, ...)
         * @param classLocation the package location of the class the subject is a member of
         * @param causes a list of cause-descriptions which explain what causes the deviation (e.g. wrong visibility)
         * @return the fully assembled deviation object encapsulating all the information
         */
        fun buildMisimplementedDeviation(
            level: DeviationLevel,
            area: DeviationArea,
            affectedClassName: String,
            subject: DeviationSubject,
            subjectName: String,
            classLocation: String,
            causes: List<String>,
            designDiagramPath: String,
            implPath: String,
        ): Deviation {
            val type = DeviationType.MISIMPLEMENTED
            return Deviation(
                level,
                area,
                type,
                listOf(affectedClassName),
                "Deviating ${subject.name.lowercase()} implementation",
                buildMisimplementedDescription(subject, subjectName, classLocation, causes, affectedClassName),
                designDiagramPath,
                implPath.concatOverlap("${classLocation.replace(".", "/")}/$affectedClassName.java")
            )

        }

    }


}