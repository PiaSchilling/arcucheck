package classes

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

internal class ClassOccurrenceTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "ClassOccurrence"

    private val implClassA = "src/test/kotlin/testInput/klasses/a/classOccurrence/"
    private val implClassB = "src/test/kotlin/testInput/klasses/b/classOccurrence/"

    private val designClassA = "src/test/kotlin/testInput/klasses/a/classOccurrence/$testClassName.puml"
    private val designClassB = "src/test/kotlin/testInput/klasses/b/classOccurrence/$testClassName.puml"

    @Test
    fun divergentClassOccurrences_reportsDeviation_ofTypeUnexpectedAbsent() {

        val resultDeviationsA = controller.onExecuteCommandTest(implClassA, designClassB, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implClassB, designClassA, TEST)

        assert(resultDeviationsA.any { deviation -> deviation.deviationType == DeviationType.UNEXPECTED })
        assert(resultDeviationsA.any { deviation -> deviation.subjectType == DeviationSubjectType.CLASS })
        assert(resultDeviationsA.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsA.any { deviation -> deviation.affectedClassesNames.contains("${testClassName}B")})

        assert(resultDeviationsB.any { deviation -> deviation.deviationType == DeviationType.ABSENT })
        assert(resultDeviationsB.any { deviation -> deviation.subjectType == DeviationSubjectType.CLASS })
        assert(resultDeviationsB.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsB.any { deviation -> deviation.affectedClassesNames.contains("${testClassName}B")})
    }

    @Test
    fun convergentClassOccurrences_reportsNoDeviation() {
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