package core.impl

import core.model.deviation.*

class WarningBuilder {

    companion object {
        private fun buildUnexpectedAbsentDescription(
            deviationType: DeviationType,
            deviationSubject: DeviationSubject,
            subjectName: String,
            classLocation: String
        ): String {
            val deviationLocation =
                "${deviationSubject.asString} \"${subjectName}\" located in \"${classLocation}\""
            return when (deviationType) {
                DeviationType.UNEXPECTED -> "$deviationLocation is not expected according to the design but present in the implementation."
                DeviationType.ABSENCE -> "$deviationLocation is expected according to the design but not present in the implementation." // TODO revise message: man kommt durcheinander was jetzt design klassen sind und was impl klassen sind
                else -> ""
            }
        }

        fun buildUnexpectedAbsentDeviation(
            level: DeviationLevel,
            area: DeviationArea,
            type: DeviationType,
            affectedClassesNames: List<String>,
            subject: DeviationSubject,
            subjectName: String,
            classLocation: String
        ): Deviation {
            return Deviation(
                level,
                area,
                type,
                affectedClassesNames,
                "${type.asAdjective} ${subject.name.lowercase()}",
                buildUnexpectedAbsentDescription(
                    type,
                    subject,
                    subjectName,
                    classLocation
                )
            )
        }


        private fun buildMisimplementedDescription(
            deviationSubject: DeviationSubject,
            subjectName: String,
            classLocation: String,
            deviationCauses: List<String>
        ): String {
            return "Implementation of ${deviationSubject.name.lowercase()} \"$subjectName\" in class \"$classLocation\" " +
                    "deviates from the design: $deviationCauses"
        }

        fun buildMisimplementedDeviation(
            level: DeviationLevel,
            area: DeviationArea,
            affectedClassesNames: List<String>,
            subject: DeviationSubject,
            subjectName: String,
            classLocation: String,
            causes: List<String>
        ): Deviation {
            val type = DeviationType.MISIMPLEMENTED
            return Deviation(
                level,
                area,
                type,
                affectedClassesNames,
                "Deviating ${subject.name.lowercase()} implementation",
                buildMisimplementedDescription(subject, subjectName, classLocation, causes)
            )
        }

    }


}