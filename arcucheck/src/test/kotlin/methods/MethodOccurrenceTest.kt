package methods

import control.api.Controller
import control.di.controlModule
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

internal class MethodOccurrenceTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "MethodOccurrence"

    private val implClassA = "src/test/kotlin/testInput/methods/a/$testClassName.java"
    private val implClassB = "src/test/kotlin/testInput/methods/b/$testClassName.java"

    private val designClassA = "src/test/kotlin/testInput/methods/a/$testClassName.puml"
    private val designClassB = "src/test/kotlin/testInput/methods/b/$testClassName.puml"

    @Test
    fun divergentMethodOccurrences_reportsDeviation_ofTypeUnexpectedAbsent() {

        val resultDeviationsA = controller.onExecuteCommandTest(implClassA, designClassB, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implClassB, designClassA, TEST)

        assert(resultDeviationsA.size == 1)
        assert(resultDeviationsB.size == 1)

        val resultDeviationA = resultDeviationsA[0]
        val resultDeviationB = resultDeviationsB[0]

        assert(resultDeviationA.deviationType == DeviationType.UNEXPECTED)
        assert(resultDeviationA.subjectType == DeviationSubjectType.METHOD)
        assert(resultDeviationA.level == DeviationLevel.MIKRO)
        assert(resultDeviationA.affectedClassesNames.contains(testClassName))

        assert(resultDeviationB.deviationType == DeviationType.ABSENT)
        assert(resultDeviationB.subjectType == DeviationSubjectType.METHOD)
        assert(resultDeviationB.level == DeviationLevel.MIKRO)
        assert(resultDeviationB.affectedClassesNames.contains(testClassName))
    }

    @Test
    fun convergentMethodOccurrences_reportsNoDeviation() {
        assertEquals(emptyList(), controller.onExecuteCommandTest(implClassA, designClassA, TEST))
        assertEquals(emptyList(), controller.onExecuteCommandTest(implClassB, designClassB, TEST))
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