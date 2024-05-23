package control.di

import com.google.inject.AbstractModule
import control.api.Controller
import control.impl.ControllerImpl

class ControlModule : AbstractModule() {
    override fun configure() {
        bind(Controller::class.java).to(ControllerImpl::class.java)
    }
}