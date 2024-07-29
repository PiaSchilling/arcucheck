package fields

import control.api.Controller
import control.di.controlModule
import core.constants.TEST
import core.di.coreModule
import core.model.deviation.Deviation
import core.model.deviation.DeviationLevel
import core.model.deviation.DeviationSubjectType
import core.model.deviation.DeviationType
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertEquals

internal class FieldVisibilityTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "FieldVisibility"

    private val implPublicField = "src/test/kotlin/testInput/fields/b/$testClassName.java"
    private val implPrivateField = "src/test/kotlin/testInput/fields/a/$testClassName.java"

    private val designPublicField = "src/test/kotlin/testInput/fields/b/${testClassName}.puml"
    private val designPrivateField = "src/test/kotlin/testInput/fields/a/${testClassName}.puml"

    @Test
    fun divergentVisibility_reportsDeviation_ofTypeMisimplemented() {

        val resultDeviationsA = controller.onExecuteCommandTest(implPrivateField, designPublicField, TEST)
        val resultDeviationsB = controller.onExecuteCommandTest(implPublicField, designPrivateField, TEST)

        assert(resultDeviationsA.size == 1)
        assert(resultDeviationsB.size == 1)

        val resultDeviationA = resultDeviationsA[0]
        val resultDeviationB = resultDeviationsB[0]

        assert(resultDeviationA.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationA.subjectType == DeviationSubjectType.FIELD)
        assert(resultDeviationA.level == DeviationLevel.MIKRO)
        assert(resultDeviationA.affectedClassesNames.contains(testClassName))
        assert(resultDeviationA.description.contains("visibility"))

        assert(resultDeviationB.deviationType == DeviationType.MISIMPLEMENTED)
        assert(resultDeviationB.subjectType == DeviationSubjectType.FIELD)
        assert(resultDeviationB.level == DeviationLevel.MIKRO)
        assert(resultDeviationB.affectedClassesNames.contains(testClassName))
        assert(resultDeviationB.description.contains("visibility"))
    }

    @Test
    fun convergentVisibility_reportsNoDeviation() {
        assertEquals(emptyList(), controller.onExecuteCommandTest(implPrivateField, designPrivateField, TEST))
        assertEquals(emptyList(), controller.onExecuteCommandTest(implPublicField, designPublicField, TEST))
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