package core.di

import core.api.CodeParser
import core.api.UMLMapper
import core.impl.CodeParserImpl
import core.impl.UMLMapperImpl
import org.koin.dsl.module


val coreModule = module {
    single<CodeParser> { CodeParserImpl() }
    single<UMLMapper> { UMLMapperImpl() }
}
