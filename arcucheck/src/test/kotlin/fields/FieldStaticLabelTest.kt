package fields

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

internal class FieldStaticLabelTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "FieldStaticLabel"

    private val implFieldA = "src/test/kotlin/testInput/fields/a/$testClassName.java"
    private val implFieldB = "src/test/kotlin/testInput/fields/b/$testClassName.java"

    private val designFieldA = "src/test/kotlin/testInput/fields/a/$testClassName.puml"
    private val designFieldB = "src/test/kotlin/testInput/fields/b/$testClassName.puml"

    @Test
    fun divergentStaticLabel_reportsDeviation_ofTypeMisimplemented() {

        val resultDeviationsA = controller.onExecuteCommandTest(implFieldA, designFieldB, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implFieldB, designFieldA, TEST)

        assert(resultDeviationsA.size == 1)
        assert(resultDeviationsB.size == 1)

        val resultDeviationA = resultDeviationsA[0]
        val resultDeviationB = resultDeviationsB[0]

        assert(resultDeviationA.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationA.subjectType == DeviationSubjectType.FIELD)
        assert(resultDeviationA.level == DeviationLevel.MIKRO)
        assert(resultDeviationA.affectedClassesNames.contains(testClassName))
        assert(resultDeviationA.description.contains("static"))

        assert(resultDeviationB.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationB.subjectType == DeviationSubjectType.FIELD)
        assert(resultDeviationB.level == DeviationLevel.MIKRO)
        assert(resultDeviationB.affectedClassesNames.contains(testClassName))
        assert(resultDeviationB.description.contains("static"))

    }

    @Test
    fun convergentStaticLabel_reportsNoDeviation() {
        assertEquals(emptyList(), controller.onExecuteCommandTest(implFieldA, designFieldA, TEST))
        assertEquals(emptyList(), controller.onExecuteCommandTest(implFieldB, designFieldB, TEST))
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