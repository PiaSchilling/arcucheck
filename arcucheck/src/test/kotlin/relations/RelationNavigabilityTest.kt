package relations

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

internal class RelationNavigabilityTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "RelationNavigability"

    private val implClassA = "src/test/kotlin/testInput/relations/a/navigability/"
    private val implClassB = "src/test/kotlin/testInput/relations/b/navigability/"

    private val designClassA = "src/test/kotlin/testInput/relations/a/navigability/$testClassName.puml"
    private val designClassB = "src/test/kotlin/testInput/relations/b/navigability/$testClassName.puml"

    @Test
    fun divergentNavigabilityRelation_reportsDeviation_ofTypeMisimplemented() {

        val resultDeviationsA = controller.onExecuteCommandTest(implClassA, designClassB, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implClassB, designClassA, TEST)

        assert(resultDeviationsA.any { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED })
        assert(resultDeviationsA.any { deviation -> deviation.subjectType == DeviationSubjectType.RELATION })
        assert(resultDeviationsA.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsA.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})
        assert(resultDeviationsA.any { deviation -> deviation.description.contains("NAVIGABILITY")})

        assert(resultDeviationsB.any { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED })
        assert(resultDeviationsB.any { deviation -> deviation.subjectType == DeviationSubjectType.RELATION })
        assert(resultDeviationsB.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsB.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})
        assert(resultDeviationsA.any { deviation -> deviation.description.contains("NAVIGABILITY")})

    }

    @Test
    fun convergentNavigabilityRelation_reportsNoDeviation() {
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