package control.di

import control.api.Controller
import control.impl.ControllerImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val controlModule = module {
    singleOf(::ControllerImpl) bind Controller::class
}
