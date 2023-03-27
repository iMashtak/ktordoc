package com.example.plugins

import io.github.imashtak.ktordoc.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureOpenAPI() {
    routing {
        generateOpenAPI() {
            info {
                title("Example API")
                version("1.0.0")
            }
            server {
                url("http://127.0.0.1:8080")
            }
            security("Bearer")
            securitySchema("Bearer") {
                basic()
            }
        }
        swaggerUI(path = "openapi")
    }
}
