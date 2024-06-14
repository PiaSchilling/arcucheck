package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLClass
import core.model.puml.PUMLField

class FieldComparator {

    fun comparePUMLFields(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>() // TODO duplicate code in method comparator

        val implClassesMap = implementationClasses.associateBy { it.fullName }
        val designClassesMap = designClasses.associateBy { it.fullName }

        val correctClassesNames = implClassesMap.keys.intersect(designClassesMap.keys)

        // Only check fields for correct classes (correct means: class signatures don't have any deviations)
        correctClassesNames.forEach { correctClassName ->
            val correctImplClass = implClassesMap[correctClassName]
            val correctDesignClass = designClassesMap[correctClassName]

            if (correctImplClass != null && correctDesignClass != null) {
                deviations.addAll(checkFields(correctImplClass, correctDesignClass))
            }

        }
        return deviations
    }

    private fun checkFields(
        implementationClass: PUMLClass,
        designClass: PUMLClass
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        val maybeAbsentFields = designClass.fields.subtract(implementationClass.fields.toSet())
        val maybeUnexpectedFields = implementationClass.fields.subtract(designClass.fields.toSet())

        if (maybeAbsentFields.isNotEmpty()) {
            val absentOrWrongFields = checkDeviatingFields(
                maybeAbsentFields.toList(),
                implementationClass,
                designClass,
                DeviationType.ABSENCE
            )
            deviations.addAll(absentOrWrongFields)
        }
        if (maybeUnexpectedFields.isNotEmpty()) {
            val unexpectedOrWrongFields = checkDeviatingFields(
                maybeUnexpectedFields.toList(),
                implementationClass,
                designClass,
                DeviationType.UNEXPECTED
            )
            deviations.addAll(unexpectedOrWrongFields)
        }

        return deviations
    }

    private fun checkDeviatingFields(
        deviatingFields: List<PUMLField>,
        implementationClass: PUMLClass,
        designClass: PUMLClass,
        deviationType: DeviationType
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

        val deviatingFieldsMap = deviatingFields.associateBy { it.name }
        val implementationFieldsMap = implementationClass.fields.associateBy { it.name }
        val designFieldsMap = designClass.fields.associateBy { it.name }

        deviatingFieldsMap.forEach { field ->
            val match = when (deviationType) {
                DeviationType.ABSENCE -> implementationFieldsMap[field.key]
                DeviationType.UNEXPECTED -> designFieldsMap[field.key]
                else -> implementationFieldsMap[field.key]
            }
            match?.let {
                // prevent bidirectional/duplicate adding of deviations -> only execute block below if match is a designClass
                // -> match is designClass if the type is UNEXPECTED
                if (deviationType == DeviationType.UNEXPECTED) {
                    val deviationCauses = checkDeviationArea(field.value, match)
                    deviations.add(
                        Deviation(
                            DeviationLevel.MIKRO,
                            DeviationArea.PROPERTY,
                            DeviationType.MISIMPLEMENTED,
                            listOf(designClass.name),
                            "Wrong implemented field", // TODO clean up message
                            "Field ${field.value.name} in class ${designClass.name} is implemented incorrectly: $deviationCauses"
                        )
                    )
                }
            } ?: run {
                deviations.add( // If method still can not be found, then it will be marked as absent/unexpected
                    Deviation(
                        DeviationLevel.MIKRO,
                        DeviationArea.PROPERTY,
                        deviationType,
                        listOf(designClass.name),
                        "$deviationType field", // TODO fix messages!!!
                        "Field ${field.value.name} in class ${designClass.name} is $deviationType"
                    )
                )
            }
        }
        return deviations
    }

    private fun checkDeviationArea(
        implementationField: PUMLField,
        designField: PUMLField
    ): List<String> {
        val fieldWarnings = mutableListOf<String>()

        if (implementationField.dataType != designField.dataType) {
            fieldWarnings.add(
                "Field ${designField.name} should have the data type ${designField.dataType} but has the" +
                        "data type ${implementationField.dataType}"
            )
        }

        if (implementationField.visibility != designField.visibility) {
            fieldWarnings.add(
                "Field ${designField.name} should have the visibility ${designField.visibility} but has the" +
                        "visibility ${implementationField.visibility}"
            )
        }

        if (implementationField.isStatic && !designField.isStatic) {
            fieldWarnings.add(
                "Field ${designField.name} should not be static according to the design but is static in " +
                        "the implementation"
            )
        } else if (!implementationField.isStatic && designField.isStatic) {
            fieldWarnings.add(
                "Field ${designField.name} should be static according to the design but is not static in the " +
                        "implementation"
            )
        }

        return fieldWarnings
    }
}