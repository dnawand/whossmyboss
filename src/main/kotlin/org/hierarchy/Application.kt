package org.hierarchy

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.server.netty.NettyApplicationEngine
import io.micronaut.ktor.KtorApplication
import io.micronaut.ktor.KtorApplicationBuilder
import io.micronaut.ktor.runApplication
import jakarta.inject.Singleton

@Singleton
class KtorApp : KtorApplication<NettyApplicationEngine.Configuration>({
})

@Singleton
class KtorConfiguration : KtorApplicationBuilder({
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Authentication) {
        basic("auth-basic") {
            realm = ""
            validate { credentials ->
                if (credentials.name == "user" && credentials.password == "password") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
})

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        runApplication(args)
    }
}
