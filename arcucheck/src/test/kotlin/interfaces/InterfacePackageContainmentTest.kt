package interfaces

import control.api.Controller
import control.di.controlModule
import core.constants.RELEASE
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

internal class InterfacePackageContainmentTest : KoinTest {
    private val controller by inject<Controller>()

    private val testClassName = "InterfacePackageContainment"

    private val implClassA = "src/test/kotlin/testInput/interfazes/a/$testClassName.java"
    private val implClassB = "src/test/kotlin/testInput/interfazes/b/$testClassName.java"

    private val designClassA = "src/test/kotlin/testInput/interfazes/a/$testClassName.puml"
    private val designClassB = "src/test/kotlin/testInput/interfazes/b/$testClassName.puml"

    @Test
    fun divergentPackageContainment_reportsDeviation_ofTypeMisimplemented() {

        val resultDeviationsA = controller.onExecuteCommandTest(implClassA, designClassB, RELEASE)
        val resultDeviationsB = controller.onExecuteCommandTest(implClassB, designClassA, RELEASE)

        println(resultDeviationsA)

        assert(resultDeviationsA.any { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED })
        assert(resultDeviationsA.any { deviation -> deviation.subjectType == DeviationSubjectType.INTERFACE })
        assert(resultDeviationsA.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsA.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})
        assert(resultDeviationsA.any { deviation -> deviation.description.contains("wrong package")})

        assert(resultDeviationsB.any { deviation -> deviation.deviationType == DeviationType.MISIMPLEMENTED })
        assert(resultDeviationsB.any { deviation -> deviation.subjectType == DeviationSubjectType.INTERFACE })
        assert(resultDeviationsB.any { deviation -> deviation.level == DeviationLevel.MAKRO })
        assert(resultDeviationsB.any { deviation -> deviation.affectedClassesNames.contains(testClassName)})
        assert(resultDeviationsB.any { deviation -> deviation.description.contains("wrong package")})
    }

    @Test
    fun convergentPackageContainment_reportsNoDeviation() {
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