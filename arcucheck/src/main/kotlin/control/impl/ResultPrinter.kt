package control.impl

import core.model.deviation.Deviation
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationSubjectType
import core.model.deviation.DeviationType
import kotlin.system.exitProcess

class ResultPrinter {
    companion object{
        fun printResults(fileCount: Int, deviations:List<Deviation>){
            println("===== Deviation analysis result ===== \n")
            println("Compared PlantUML design files to their related implementation: $fileCount\n")
            println("----------------------------------------------------------------------------------\n")
            if (deviations.isEmpty()) {
                println("No deviations between design and implementation found.")
                exitProcess(0)
            } else {
                printStatistics(deviations)
                println("----------------------------------------------------------------------------------\n")
                printDeviations(deviations)
                println("----------------------------------------------------------------------------------\n")
                exitProcess(1)
            }
        }

        /**
         * Print statistics about the provided deviations
         *
         * @param deviations for which the stats should be printed
         */
        private fun printStatistics(deviations: List<Deviation>) {
            val makroDeviationCount = deviations.count { deviation -> deviation.level == DeviationLevel.MAKRO }
            val mikroDeviationCount = deviations.count { deviation -> deviation.level == DeviationLevel.MIKRO }

            val absentDeviationCount = deviations.count { deviation -> deviation.deviationType == DeviationType.ABSENT }
            val unexpectedDeviationCount =
                deviations.count { deviation -> deviation.deviationType == DeviationType.UNEXPECTED }
            val misimplementedDeviationCount =
                deviations.count { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED }

            val relationDeviationCount =
                deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.RELATION }
            val packageDeviationCount =
                deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.PACKAGE }
            val classDeviationCount =
                deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.CLASS }
            val interfaceDeviationCount =
                deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.INTERFACE }
            val methodDeviationCount =
                deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.METHOD }
            val constructorDeviationCount =
                deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.CONSTRUCTOR }
            val fieldDeviationCount =
                deviations.count { deviation -> deviation.subjectType == DeviationSubjectType.FIELD }


            println("Total deviations found: ${deviations.size}")
            println()

            println("Breakdown by deviation LEVEL")
            println("Level   Description                                                 Count")
            println("MAKRO   Architectural level, impacting overall structure and design $makroDeviationCount deviations")
            println("MIKRO   Code level, affecting specific implementations and details  $mikroDeviationCount deviations")
            println()

            println("Breakdown by deviation TYPE")
            println("Type             Description                                                                            Count")
            println("ABSENT           Subject expected in the design but missing in the implementation                       $absentDeviationCount deviations")
            println("UNEXPECTED       Subject not expected in the design but present in the implementation                   $unexpectedDeviationCount deviations")
            println("MISIMPLEMENTED   Subject expected in the design, present in the implementation but wrongly implemented  $misimplementedDeviationCount deviations")
            println()

            println("Breakdown by deviation SUBJECT TYPE")
            println("Subject type   Description                        Count")
            println("RELATION       Deviations affecting relations     $relationDeviationCount deviations")
            println("PACKAGE        Deviations affecting packages      $packageDeviationCount deviations")
            println("CLASS          Deviations affecting classes       $classDeviationCount deviations")
            println("INTERFACE      Deviations affecting interfaces    $interfaceDeviationCount deviations")
            println("METHOD         Deviations affecting methods       $methodDeviationCount deviations")
            println("CONSTRUCTOR    Deviations affecting constructors  $constructorDeviationCount deviations")
            println("FIELD          Deviations affecting fields        $fieldDeviationCount deviations")
            println()


        }

        /**
         * Print the provided deviations
         *
         * @param deviations that should be printed
         */
        private fun printDeviations(deviations: List<Deviation>){
            println("List of detected deviations:")
            println(deviations.joinToString("\n"))
        }
    }
}