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

internal class MethodAbstractLabelTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "MethodAbstractLabel"

    private val implMethodA = "src/test/kotlin/testInput/methods/a/$testClassName.java"
    private val implMethodB = "src/test/kotlin/testInput/methods/b/$testClassName.java"

    private val designMethodA = "src/test/kotlin/testInput/methods/a/$testClassName.puml"
    private val designMethodB = "src/test/kotlin/testInput/methods/b/$testClassName.puml"

    @Test
    fun divergentAbstractLabel_reportsDeviation_ofTypeMisimplemented() {

        val resultDeviationsA = controller.onExecuteCommandTest(implMethodA, designMethodB, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implMethodB, designMethodA, TEST)

        assert(resultDeviationsA.any { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED })
        assert(resultDeviationsA.any { deviation -> deviation.subjectType == DeviationSubjectType.METHOD })
        assert(resultDeviationsA.any { deviation -> deviation.level == DeviationLevel.MIKRO })
        assert(resultDeviationsA.any { deviation -> deviation.affectedClassesNames.contains(testClassName) })
        assert(resultDeviationsA.any { deviation -> deviation.description.contains("abstract") })

        assert(resultDeviationsB.any { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED })
        assert(resultDeviationsB.any { deviation -> deviation.subjectType == DeviationSubjectType.METHOD })
        assert(resultDeviationsB.any { deviation -> deviation.level == DeviationLevel.MIKRO })
        assert(resultDeviationsB.any { deviation -> deviation.affectedClassesNames.contains(testClassName) })
        assert(resultDeviationsB.any { deviation -> deviation.description.contains("abstract") })
    }

    @Test
    fun convergentAbstractLabel_reportsNoDeviation() {
        assertEquals(emptyList(), controller.onExecuteCommandTest(implMethodA, designMethodA, TEST))
        assertEquals(emptyList(), controller.onExecuteCommandTest(implMethodB, designMethodB, TEST))
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