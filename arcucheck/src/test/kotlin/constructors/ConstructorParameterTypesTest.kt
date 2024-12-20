package constructors

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

internal class ConstructorParameterTypesTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "ConstructorParameterTypes"

    private val implConstructorA = "src/test/kotlin/testInput/constructors/a/$testClassName.java"
    private val implConstructorB = "src/test/kotlin/testInput/constructors/b/$testClassName.java"

    private val designConstructorA = "src/test/kotlin/testInput/constructors/a/${testClassName}.puml"
    private val designConstructorB = "src/test/kotlin/testInput/constructors/b/${testClassName}.puml"

    @Test
    fun divergentParameterTypes_reportsDeviation_ofTypeMisimplemented() {

        val resultDeviationsA = controller.onExecuteCommandTest(implConstructorA, designConstructorB, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implConstructorB, designConstructorA, TEST)

        assert(resultDeviationsA.size == 1)
        assert(resultDeviationsB.size == 1)

        val resultDeviationA = resultDeviationsA[0]
        val resultDeviationB = resultDeviationsB[0]

        assert(resultDeviationA.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationA.subjectType == DeviationSubjectType.CONSTRUCTOR)
        assert(resultDeviationA.level == DeviationLevel.MIKRO)
        assert(resultDeviationA.affectedClassesNames.contains(testClassName))
        assert(resultDeviationA.description.contains("parameter types"))

        assert(resultDeviationB.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationB.subjectType == DeviationSubjectType.CONSTRUCTOR)
        assert(resultDeviationB.level == DeviationLevel.MIKRO)
        assert(resultDeviationB.affectedClassesNames.contains(testClassName))
        assert(resultDeviationB.description.contains("parameter types"))
    }

    @Test
    fun convergentParameterTypes_reportsNoDeviation() {
        assertEquals(emptyList(), controller.onExecuteCommandTest(implConstructorA, designConstructorA, TEST))
        assertEquals(emptyList(), controller.onExecuteCommandTest(implConstructorB, designConstructorB, TEST))
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