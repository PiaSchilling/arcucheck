package core.di

import core.api.CodeParser
import core.impl.CodeParserImpl
import org.koin.dsl.module


val coreModule = module {
    single<CodeParser>{ CodeParserImpl()}
}
