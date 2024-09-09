package core.impl.comparators

import core.impl.DeviationBuilder
import core.model.deviation.*
import core.model.puml.PUMLClass
import core.model.puml.PUMLField

/**
 * Compares the design of Fields to the according implementation to detect any deviations
 *
 * @constructor just initializes fields, those are only necessary, so they can be added to the
 * output for more precise warnings
 *
 *
 * @param designDiagramPath the path to the diagram representing the design
 * @param implPath the path to the implementation which is compared to the design
 */
class FieldComparator(private val designDiagramPath: String, private val implPath: String) {


    /**
     * Compare all fields contained in the provided classes to detect any deviations.
     * Only fields for "correct" classes are compared. Correct means, that class signatures don't have any deviations.
     *
     * @param implementationClasses the classes as they exist in the implementation
     * @param designClasses the same classes as present in the design
     * @return a list of all detected deviations between the design and implementation classes fields
     */
    fun comparePUMLFields(
        implementationClasses: List<PUMLClass>,
        designClasses: List<PUMLClass>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()

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

    /**
     * Check if any fields of the provided classes are unexpected, absent of wrongly implemented
     *
     * @param implementationClass a class as it exists in the implementation
     * @param designClass the same class as is present in the design
     * @return a list of all detected deviations between the design and implementation classes fields
     */
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
                DeviationType.ABSENT
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

    /**
     * Find the deviation cause of fields: Deviation could be caused by absent field, unexpected field or wrong implemented field
     *
     * @param deviatingFields a list of fields which deviate from the design, that need to be checked
     * @param implementationClass the implemented class containing the deviating fields
     * @param designClass the design of the class containing the deviating fields
     * @param deviationType fields can either be suspected to be ABSENT or UNEXPECTED, deviationType controls the behavior of this function
     * @return a list containing all detected deviations
     */
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
                DeviationType.ABSENT -> implementationFieldsMap[field.key]
                DeviationType.UNEXPECTED -> designFieldsMap[field.key]
                else -> implementationFieldsMap[field.key]
            }
            match?.let {
                // prevent bidirectional/duplicate adding of deviations -> only execute block below if match is a designClass
                // -> match is designClass if the type is UNEXPECTED
                if (deviationType == DeviationType.UNEXPECTED) {
                    val deviationCauses = findDeviationCauses(field.value, match)
                    deviations.add(
                        DeviationBuilder.buildMisimplementedDeviation(
                            level = DeviationLevel.MIKRO,
                            subjectType = DeviationSubjectType.FIELD,
                            affectedClassName = designClass.name,
                            subjectName = field.value.name,
                            classLocation = designClass.pumlPackage.fullName,
                            causes = deviationCauses,
                            designDiagramPath = designDiagramPath,
                            implPath = implPath,
                        )
                    )
                }
            } ?: run {
                deviations.add( // If method still can not be found, then it will be marked as absent/unexpected
                    DeviationBuilder.buildUnexpectedAbsentDeviation(
                        level = DeviationLevel.MIKRO,
                        subjectType = DeviationSubjectType.FIELD,
                        deviationType = deviationType,
                        affectedClassName = designClass.name,
                        subjectName = field.value.name,
                        classLocation = designClass.pumlPackage.fullName,
                        designDiagramPath = designDiagramPath,
                        implPath = implPath,
                    )
                )
            }
        }
        return deviations
    }

    /**
     * Check in which area a field deviates from the design (wrong visibility, missing abstract or static modifier)
     * Could also be multiple areas (e.g. wrong visibility and missing abstract modifier)
     *
     * @param implementationField the implemented field (is-state)
     * @param designField intended design of the field (should-state)
     * @return a list containing all detected deviations
     */
    private fun findDeviationCauses(
        implementationField: PUMLField,
        designField: PUMLField
    ): List<String> {
        val fieldWarnings = mutableListOf<String>()

        if (implementationField.dataType != designField.dataType) {
            fieldWarnings.add(
                "Should have the data type ${designField.dataType} according to the design but has the" +
                        "data type ${implementationField.dataType} in the implementation."
            )
        }

        if (implementationField.visibility != designField.visibility) {
            fieldWarnings.add(
                "Should have the visibility ${designField.visibility} according to the design but has the " +
                        "visibility ${implementationField.visibility} in the implementation."
            )
        }

        if (implementationField.isStatic && !designField.isStatic) {
            fieldWarnings.add(
                "Is marked as static in the implementation but should not be static according to the design."
            )
        } else if (!implementationField.isStatic && designField.isStatic) {
            fieldWarnings.add(
                "Should be static according to the design but is not marked as static in the implementation."
            )
        }

        return fieldWarnings
    }
}