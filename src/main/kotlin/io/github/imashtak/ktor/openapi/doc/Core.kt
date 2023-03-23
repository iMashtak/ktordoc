package io.github.imashtak.ktor.openapi.doc

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter
import java.io.File

/**
 * Attaches additional OpenAPI information to the wrapped route.
 */
fun Route.api(op: OperationModel.() -> Unit, block: Route.() -> Route) {
    val route = block.invoke(this)
    val model = OperationModel().apply(op)
    route.attributes.put(OperationModelAttribute, model)
}

val OperationModelAttribute = AttributeKey<OperationModel>("openapi-operation-route-document")

/**
 * Generates OpenAPI specification based on defined routes.
 */
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
    val converter = ModelConverters.getInstance()
    val paths = HashMap<String, PathItem>()
    for (route in routes) {
        if (!route.attributes.contains(OperationModelAttribute)) {
            continue
        }
        val routeInfo = RouteInfo()
        analyzeRoute(routeInfo, route)
        val operation = route.attributes[OperationModelAttribute].operation
        processParameters(converter, operation, routeInfo.pathParameters, "path")
        processParameters(converter, operation, routeInfo.queryParameters, "query")
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

private fun processParameters(
    converter: ModelConverters,
    operation: Operation,
    parameters: List<Pair<String, Boolean>>,
    type: String
) {
    for ((parameter, required) in parameters) {
        if (operation.parameters == null) operation.parameters = mutableListOf()
        if (operation.parameters.any { it.name == parameter }) {
            val x = operation.parameters.filter { it.name == parameter }[0]
            if (x.required == null) {
                x.required(if (required) true else null)
            }
            if (x.`in` == null) {
                x.`in`(type)
            }
            if (x.schema == null && x.content == null) {
                x.schema(converter.resolveAsResolvedSchema(AnnotatedType(String::class.java)).schema)
            }
        } else {
            operation.parameters.add(Parameter()
                .name(parameter)
                .required(if (required) true else null)
                .`in`(type)
                .schema(converter.resolveAsResolvedSchema(AnnotatedType(String::class.java)).schema)
            )
        }
    }
}

private class RouteInfo {
    var url: String = ""
    var method: HttpMethod? = null
    val pathParameters: MutableList<Pair<String, Boolean>> = mutableListOf()
    val queryParameters: MutableList<Pair<String, Boolean>> = mutableListOf()
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
        routeInfo.url = "${selector.prefix ?: ""}{${selector.name}}${selector.suffix ?: ""}/" + routeInfo.url
        routeInfo.pathParameters.add(Pair(selector.name, true))
    }
    if (selector is PathSegmentOptionalParameterRouteSelector) {
        routeInfo.url = "${selector.prefix ?: ""}{${selector.name}}${selector.suffix ?: ""}/" + routeInfo.url
        routeInfo.pathParameters.add(Pair(selector.name, false))
    }
    if (selector is ParameterRouteSelector) {
        routeInfo.queryParameters.add(Pair(selector.name, true))
    }
    if (selector is OptionalParameterRouteSelector) {
        routeInfo.queryParameters.add(Pair(selector.name, false))
    }
    val parent = route.parent
    if (parent != null) {
        analyzeRoute(routeInfo, parent)
    }
}