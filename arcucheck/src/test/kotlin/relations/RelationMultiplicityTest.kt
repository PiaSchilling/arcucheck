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

internal class RelationMultiplicityTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "RelationMultiplicity"

    private val implClassA = "src/test/kotlin/testInput/relations/a/multiplicity/"
    private val implClassB = "src/test/kotlin/testInput/relations/b/multiplicity/"

    private val designClassA = "src/test/kotlin/testInput/relations/a/multiplicity/$testClassName.puml"
    private val designClassB = "src/test/kotlin/testInput/relations/b/multiplicity/$testClassName.puml"

    @Test
    fun divergentMultiplicityRelation_reportsDeviation_ofTypeUnexpectedAbsent() {

        val resultDeviationsA = controller.onExecuteCommandTest(implClassA, designClassB, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implClassB, designClassA, TEST)

        assert(resultDeviationsA.any { deviation -> deviation.deviationType == DeviationType.UNEXPECTED })
        assert(resultDeviationsA.any { deviation -> deviation.subjectType == DeviationSubjectType.RELATION })
        assert(resultDeviationsA.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsA.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})
        assert(resultDeviationsA.any { deviation -> deviation.description.contains("MULTIPLICITY")})

        assert(resultDeviationsB.any { deviation -> deviation.deviationType == DeviationType.ABSENT })
        assert(resultDeviationsB.any { deviation -> deviation.subjectType == DeviationSubjectType.RELATION })
        assert(resultDeviationsB.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsB.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})
        assert(resultDeviationsA.any { deviation -> deviation.description.contains("MULTIPLICITY")})

    }

    @Test
    fun convergentMultiplicityRelation_reportsNoDeviation() {
        /**
         * Multiplicity is neither extracted from the implementation nor from the design, so it is ignored in both models,
         * resulting in convergent models. Since the test aims to verify multiplicity, it cannot be considered passed
         * in any case because this aspect was overlooked.
         */
        assert(false)
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