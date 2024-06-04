package core.di

import core.api.CodeParser
import core.api.PUMLMapper
import core.impl.CodeParserImpl
import core.impl.PUMLMapperImpl
import org.koin.dsl.module


val coreModule = module {
    single<CodeParser> { CodeParserImpl() }
    single<PUMLMapper> { PUMLMapperImpl() }
}
