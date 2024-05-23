package core.di

import com.google.inject.AbstractModule
import core.api.CodeParser
import core.impl.CodeParserImpl

class CoreModule:AbstractModule() {
    override fun configure() {
        bind(CodeParser::class.java).to(CodeParserImpl::class.java)
    }
}