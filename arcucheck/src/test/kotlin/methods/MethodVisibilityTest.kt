package methods

import control.api.Controller
import control.di.controlModule
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

internal class MethodVisibilityTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "MethodVisibility"

    private val implMethodA = "src/test/kotlin/testInput/methods/a/$testClassName.java"
    private val implMethodB = "src/test/kotlin/testInput/methods/b/$testClassName.java"

    private val designMethodA = "src/test/kotlin/testInput/methods/a/$testClassName.puml"
    private val designMethodB = "src/test/kotlin/testInput/methods/b/$testClassName.puml"

    @Test
    fun divergentReturnType_reportsDeviation_ofTypeMisimplemented() {

        val resultDeviationsA =  controller.onExecuteCommandTest(implMethodA, designMethodB)
        val resultDeviationsB =  controller.onExecuteCommandTest(implMethodB, designMethodA)

        assert(resultDeviationsA.size == 1)
        assert(resultDeviationsB.size == 1)

        val resultDeviationA = resultDeviationsA[0]
        val resultDeviationB = resultDeviationsB[0]

        assert(resultDeviationA.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationA.subjectType == DeviationSubjectType.METHOD)
        assert(resultDeviationA.level == DeviationLevel.MIKRO)
        assert(resultDeviationA.affectedClassesNames.contains(testClassName))
        assert(resultDeviationA.description.contains("visibility"))

        assert(resultDeviationB.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationB.subjectType == DeviationSubjectType.METHOD)
        assert(resultDeviationB.level == DeviationLevel.MIKRO)
        assert(resultDeviationB.affectedClassesNames.contains(testClassName))
        assert(resultDeviationB.description.contains("visibility"))

    }

    @Test
    fun convergentReturnType_reportsNoDeviation() {
        assertEquals(emptyList(), controller.onExecuteCommandTest(implMethodA, designMethodA))
        assertEquals(emptyList(), controller.onExecuteCommandTest(implMethodB, designMethodB))
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