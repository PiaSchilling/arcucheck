package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationArea
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLType

class PackageComparator {
    /**
     * Check if any packages are absent or unexpected
     *
     * @param implementationClasses the classes present in the implementation (classes contain their respective package)
     * @param designClasses the classes expected by the design (classes contain their respective package)
     * @return a list of all detected deviations between the design and implementation packages
     */
    fun comparePUMLPackages(
        implementationClasses: List<PUMLType>,
        designClasses: List<PUMLType>
    ): List<Deviation> {
        val deviations = mutableListOf<Deviation>()
        val implPackages = implementationClasses.map { pumlClass -> pumlClass.pumlPackage }.distinct()
        val designPackages = designClasses.map { pumlClass -> pumlClass.pumlPackage }.distinct()

        val absentPackages = designPackages.subtract(implPackages.toSet())
        val unexpectedPackages = implPackages.subtract(designPackages.toSet())


        if (absentPackages.isNotEmpty()) {
            absentPackages.forEach { absentPackage ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.PROPERTY,
                        DeviationType.ABSENCE,
                        listOf(absentPackage.fullName),
                        "Missing package",
                        "Package \"${absentPackage.fullName}\" is expected according to the design but missing in the implementation."
                    )
                )
            }
        }
        if (unexpectedPackages.isNotEmpty()) {
            unexpectedPackages.forEach { unexpectedPackage ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationArea.PROPERTY,
                        DeviationType.UNEXPECTED,
                        listOf(unexpectedPackage.fullName),
                        "Unexpected package",
                        "Package \"${unexpectedPackage.fullName}\" is not expected according to the design but present in the implementation."
                    )
                )
            }
        }

        return deviations
    }
}