package com.example.plugins

import com.example.openapigen.openapi
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    routing {
        openapi({
            summary("hello world")
        }) {
            get("/") {
                call.respondText("Hello World!")
            }
        }
        openapi({
            summary("summary")
            requestBody {
                mediaType("application/json") {
                    description("")
                    schema(String::class)
                }
            }
            response("200") {
                mediaType("application/json") {
                    description("")
                    schema(Example::class)
                }
            }
        }) {
            post("/") {
                call.respond(Example("hey", 10, ExampleNested("r"), listOf()))
            }
        }
        openapi({
            summary("some")
            parameter {
                name("id")
                `in`("path")
            }
            response("200") {
                mediaType("application/json") {
                    description("")
                    schema(String::class)
                }
            }
        }) {
            put("/some/{id}") {
                call.respondText("Hello World!")
            }
        }
    }
}

@Serializable
data class Example(
    val a: String,
    val b: Int,
    val d: ExampleNested,
    val f: List<ExampleNested>
)

@Serializable
data class ExampleNested(
    val c: String
)