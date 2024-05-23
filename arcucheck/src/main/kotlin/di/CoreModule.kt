package di

import com.google.inject.AbstractModule
import control.api.Controller
import control.impl.ControllerImpl
import core.api.CodeParser
import core.impl.CodeParserImpl

class CoreModule:AbstractModule() {
    override fun configure() {
        bind(Controller::class.java).to(ControllerImpl::class.java) // TODO maybe extract to own module so it fits to package structure
        bind(CodeParser::class.java).to(CodeParserImpl::class.java)
    }
}