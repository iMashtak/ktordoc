# Ktor OpenAPI Document Generator

[Ktor](https://ktor.io) extension which generates OpenAPI document.

## Getting started

Maven dependency:

```xml
<dependency>
    <groupId>io.github.imashtak</groupId>
    <artifactId>ktor-openapi-doc</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle dependency:

```kotlin
implementation("io.github.imashtak:ktor-openapi-doc:0.1.0")
```

Use `generateOpenAPI` function to declare high level settings for OpenAPI document:

```kotlin
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
        }
    }
}
```

Then wrap endpoints with `api` function like this:

```kotlin
fun Application.configureRouting() {
    routing {
        api({
            summary("hello world")
            response("200") {
                mediaType("application/json") {
                    schema(String::class)
                }
            }
        }) {
            get("/") {
                call.respondText("\"Hello World!\"")
            }
        }
    }
}
```

Note: `generateOpenAPI` call **MUST** be placed after all routes definitions. Considering our example, `Application` module may be declared like this:

```kotlin
fun Application.module() {
    configureRouting()
    configureOpenAPI()
}
```

So now, on app starting, the file `openapi/documentation.yaml` will be created with complete OpenAPI spec of defined routes. Note that routes which are not wrapped with `api` function will not be shown in spec.

## Integrations

It is easy to integrate with [SwaggerUI](https://ktor.io/docs/swagger-ui.html) or [OpenAPI](https://ktor.io/docs/openapi.html) plugins:

```kotlin
fun Application.configureOpenAPI() {
    routing {
        generateOpenAPI(swaggerFile = "openapi.yaml") {}
        swaggerUI(swaggerFile = "openapi.yaml")
    }
}
```

## Complex Routing

There is a simple rule - `api` function must wrap "leaf node" of routes "tree". In other words you have to place `api` in the following manner:

```kotlin
fun Application.configureRouting() {
    routing {
        route("/v1") {
            api({}) {
                get("/") {
                    call.respondText("Hello World!")
                }
            }
        }
    }
}
```

## Auto parameters discovery

Cause `api` wraps "leaf node" of routes, it is possible to retrieve some more information from them. For example, path and query parameters of route. Consider the following code:

```kotlin
fun Application.configureRouting() {
    routing {
        api({}) {
            get("/some/{id}") {
                call.respondText("Hello World!")
            }
        }
    }
}
```

Parameter `id` will automatically be added to OpenAPI specification. It is possible to add some docs:

```kotlin
fun Application.configureRouting() {
    routing {
        api({
            parameter {
                name("id")
                description("description of `id` parameter")
            }
        }) {
            get("/some/{id}") {
                call.respondText("Hello World!")
            }
        }
    }
}
```

Other fields such as `in`, `required` and `schema` will be set automatically (if not set manually).

The same works for query parameters. Example uses [Resources](https://ktor.io/docs/type-safe-routing.html) plugin:

```kotlin
@Resource("/some")
class Some(val label: String?)

fun Application.configureRouting() {
    routing {
        api({
            parameter {
                name("label")
                description("description of `label` parameter")
            }
        }) {
            get<Some> {
                call.respondText("Hello World!")
            }
        }
    }
}
```

