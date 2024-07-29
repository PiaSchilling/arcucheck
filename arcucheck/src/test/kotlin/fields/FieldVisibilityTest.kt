package fields

import control.api.Controller
import control.di.controlModule
import core.di.coreModule
import core.model.deviation.Deviation
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

    private val implPublicField = "src/test/kotlin/testInput/fields/pub/FieldVisibility.java"
    private val implPrivateField = "src/test/kotlin/testInput/fields/priv/FieldVisibility.java"

    private val designPublicField = "src/test/kotlin/testInput/fields/FieldVisibilityPublic.puml"
    private val designPrivateField = "src/test/kotlin/testInput/fields/FieldVisibilityPrivate.puml"

    @Test
    fun divergentVisibility_reportsDeviation_ofTypeMisimplemented(){

    }

    @Test
    fun convergentVisibility_reportsNoDeviation(){
        assertEquals(emptyList(),controller.onExecuteCommandTest(implPrivateField,designPrivateField))
        assertEquals(emptyList(),controller.onExecuteCommandTest(implPublicField,designPublicField))
    }

    @Test
    @DisplayName("test")
    fun testFieldVisibility() {
      assertEquals(emptyList(),controller.onExecuteCommandTest(implPrivateField,designPublicField))
        val deviations = controller.onExecuteCommandTest(implPrivateField,designPublicField)
        println(deviations)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun startKoinForTest(): Unit {
            startKoin {
                modules(coreModule, controlModule)
            }
        }

        @JvmStatic
        @AfterAll
        fun stopKoinAfterTest(): Unit = stopKoin()
    }
}