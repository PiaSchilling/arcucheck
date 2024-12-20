package packages

import control.api.Controller
import control.di.controlModule
import core.constants.RELEASE
import core.constants.TEST
import core.di.coreModule
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationSubjectType
import core.model.deviation.DeviationType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertEquals

internal class PackageOccurrenceTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "PackageOccurrence"

    private val implClassA = "src/test/kotlin/testInput/packages/a/packageOccurrence/$testClassName.java"
    private val implClassB = "src/test/kotlin/testInput/packages/b/$testClassName.java"

    private val designClassA = "src/test/kotlin/testInput/packages/a/packageOccurrence/$testClassName.puml"
    private val designClassB = "src/test/kotlin/testInput/packages/b/$testClassName.puml"

    @Test
    fun divergentPackageOccurrence_reportsDeviation_ofTypeAbsentOrUnexpected() {

        val resultDeviationsA = controller.onExecuteCommandTest(implClassA, designClassB, RELEASE)
        val resultDeviationsB = controller.onExecuteCommandTest(implClassB, designClassA, RELEASE)

        assert(resultDeviationsA.any { deviation -> deviation.deviationType == DeviationType.UNEXPECTED })
        assert(resultDeviationsA.any { deviation -> deviation.subjectType == DeviationSubjectType.PACKAGE })
        assert(resultDeviationsA.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsA.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})

        assert(resultDeviationsB.any { deviation -> deviation.deviationType == DeviationType.ABSENT })
        assert(resultDeviationsB.any { deviation -> deviation.subjectType == DeviationSubjectType.PACKAGE })
        assert(resultDeviationsB.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsB.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})
    }

    @Test
    fun convergentPackageOccurrence_reportsNoDeviation() {
        assertEquals(emptyList(), controller.onExecuteCommandTest(implClassA, designClassA, RELEASE))
        assertEquals(emptyList(), controller.onExecuteCommandTest(implClassB, designClassB, RELEASE))
    }


    companion object {
        @JvmStatic
        @BeforeAll
        fun startKoinForTest() {
            startKoin {
                modules(coreModule, controlModule)
            }
        }

        @JvmStatic
        @AfterAll
        fun stopKoinAfterTest(): Unit = stopKoin()
    }
}