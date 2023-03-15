package com.example.openapigen

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import java.io.File

fun Route.openapi(op: OperationModel.() -> Unit, block: Route.() -> Route) {
    val route = block.invoke(this)
    val model = OperationModel().apply(op)
    route.attributes.put(OperationModelAttribute, model)
}

val OperationModelAttribute = AttributeKey<OperationModel>("openapi-route-documentation")

fun Routing.generateOpenAPI(
    swaggerFile: String = "openapi/documentation.yaml",
    customizer: OpenAPIModel.() -> Unit = {}
) {
    val openapiModel = OpenAPIModel().apply(customizer)
    val openapi = openapiModel.openapi

    registerPaths(openapi, getAllRoutes())

    val openapiView =
        if (swaggerFile.endsWith(".yaml")) Yaml.pretty(openapi)
        else Json.pretty(openapi)
    File(swaggerFile).writeText(openapiView)
}

private fun registerPaths(openapi: OpenAPI, routes: List<Route>) {
    val paths = HashMap<String, PathItem>()
    for (route in routes) {
        if (!route.attributes.contains(OperationModelAttribute)) {
            continue
        }
        val routeInfo = RouteInfo()
        analyzeRoute(routeInfo, route)
        val operation = route.attributes[OperationModelAttribute].operation
        var url = routeInfo.url
        if (url != "/") {
            url = url.trimEnd('/')
        }
        if (!paths.containsKey(url)) paths[url] = PathItem()
        val pathItem = paths[url]!!
        when (routeInfo.method) {
            HttpMethod.Get -> pathItem.get(operation)
            HttpMethod.Post -> pathItem.post(operation)
            HttpMethod.Put -> pathItem.put(operation)
            HttpMethod.Delete -> pathItem.delete(operation)
            HttpMethod.Patch -> pathItem.patch(operation)
            HttpMethod.Head -> pathItem.head(operation)
            HttpMethod.Options -> pathItem.options(operation)
        }
    }
    for ((name, pathItem) in paths) {
        openapi.path(name, pathItem)
    }
}

private class RouteInfo {
    var url: String = ""
    var method: HttpMethod? = null
}

private fun analyzeRoute(routeInfo: RouteInfo, route: Route) {
    val selector = route.selector
    if (selector is HttpMethodRouteSelector) {
        routeInfo.method = selector.method
    }
    if (selector is RootRouteSelector) {
        routeInfo.url = "/" + routeInfo.url
    }
    if (selector is PathSegmentConstantRouteSelector) {
        routeInfo.url = selector.value + "/" + routeInfo.url
    }
    if (selector is PathSegmentParameterRouteSelector) {
        routeInfo.url = "{${selector.name}}/" + routeInfo.url
    }
    val parent = route.parent
    if (parent != null) {
        analyzeRoute(routeInfo, parent)
    }
}