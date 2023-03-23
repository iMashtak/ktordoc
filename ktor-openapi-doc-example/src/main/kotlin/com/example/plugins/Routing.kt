package com.example.plugins

import io.github.imashtak.ktor.openapi.doc.api
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

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

@Resource("/articles")
class Articles(val sort: String)

fun Application.configureRouting() {
    routing {
        api({
            parameter {
                name("sort")
                description("Info about parameter `sort`")
            }
            response("200") {
                mediaType("text/plain") {
                    schema(String::class)
                }
            }
        }) {
            get<Articles> {
                call.respondText("Hello, sort=${it.sort}")
            }
        }
        route("/v1") {
            api({
                response("200") {
                    mediaType("application/json") {
                        schema(String::class)
                    }
                }
            }) {
                get("/x{id}y") {
                    val id = call.parameters["id"]
                    call.respondText("Hello World!, id=$id")
                }
            }
        }
        api({
            requestBody {
                mediaType("application/json") {
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
            post("/x") {
                val x = call.receiveText()
                call.respond(Example("hey", 10, ExampleNested(x), listOf()))
            }
        }
        api({
            response("200") {
                mediaType("application/json") {
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