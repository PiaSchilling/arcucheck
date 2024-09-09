package core.impl.comparators

import core.model.deviation.Deviation
import core.model.deviation.DeviationSubjectType
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationType
import core.model.puml.PUMLType

/**
 * Compares the design of packages to the according implementation to detect any deviations
 *
 * @constructor just initializes fields, those are only necessary, so they can be added to the
 * output for more precise warnings
 *
 *
 * @param designDiagramPath the path to the diagram representing the design
 * @param implPath the path to the implementation which is compared to the design
 */
class PackageComparator(private val designDiagramPath: String, private val implPath: String) {
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
                        DeviationSubjectType.PACKAGE,
                        DeviationType.ABSENT,
                        listOf(absentPackage.fullName),
                        "Missing package",
                        "Package \"${absentPackage.fullName}\" is expected according to the design but missing in the implementation.",
                        designDiagramPath,
                        implPath,
                    )
                )
            }
        }
        if (unexpectedPackages.isNotEmpty()) {
            unexpectedPackages.forEach { unexpectedPackage ->
                deviations.add(
                    Deviation(
                        DeviationLevel.MAKRO,
                        DeviationSubjectType.PACKAGE,
                        DeviationType.UNEXPECTED,
                        listOf(unexpectedPackage.fullName),
                        "Unexpected package",
                        "Package \"${unexpectedPackage.fullName}\" is not expected according to the design but present in the implementation.",
                        designDiagramPath,
                        implPath,
                    )
                )
            }
        }

        return deviations
    }
}