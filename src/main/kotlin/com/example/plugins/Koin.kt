package com.example.plugins

import com.example.di.koinModule
import io.ktor.server.application.*
import io.ktor.util.*
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin


fun Application.configureKoin(){
    install(CustomKoinPlugin.KoinPlugin){
        modules(koinModule)
    }
}


internal class CustomKoinPlugin(internal val koinApplication: KoinApplication){

   companion object KoinPlugin : Plugin<ApplicationCallPipeline, KoinApplication, CustomKoinPlugin> {
        override val key: AttributeKey<CustomKoinPlugin>
            get() = AttributeKey("CustomKoinPlugin")
        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: KoinApplication.() -> Unit
        ): CustomKoinPlugin {
            val koinApplication = startKoin(configure)
            return CustomKoinPlugin(koinApplication)
        }
    }
}

