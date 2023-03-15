package io.github.imashtak.ktor.openapi.doc

import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import kotlin.reflect.KClass

class OpenAPIModel {
    val openapi: OpenAPI = OpenAPI()

    init {
        openapi.components(components)
    }

    fun info(block: InfoModel.() -> Unit) {
        val x = InfoModel().apply(block)
        openapi.info(x.info)
    }

    fun server(block: ServerModel.() -> Unit) {
        openapi.addServersItem(ServerModel().apply(block).server)
    }
}

class InfoModel {
    val info: Info = Info()

    fun version(x: String) {
        info.version(x)
    }

    fun title(x: String) {
        info.title(x)
    }

    fun termsOfService(x: String) {
        info.termsOfService(x)
    }

    fun contact(block: ContactModel.() -> Unit) {
        info.contact(ContactModel().apply(block).contact)
    }

    fun licence(block: LicenseModel.() -> Unit) {
        info.license(LicenseModel().apply(block).license)
    }
}

class ContactModel {
    val contact: Contact = Contact()

    fun name(x: String) {
        contact.name(x)
    }

    fun url(x: String) {
        contact.url(x)
    }

    fun email(x: String) {
        contact.email(x)
    }
}

class LicenseModel {
    val license: License = License()

    fun name(x: String) {
        license.name(x)
    }

    fun url(x: String) {
        license.url(x)
    }
}

class ServerModel {
    val server: Server = Server()

    fun url(x: String) {
        server.url(x)
    }

    fun description(x: String) {
        server.description(x)
    }
}

private val components = Components()

class OperationModel {
    val operation: Operation = Operation()

    fun summary(x: String) {
        operation.summary(x)
    }

    fun description(x: String) {
        operation.description(x)
    }

    fun operationId(x: String) {
        operation.operationId(x)
    }

    fun tag(x: String) {
        operation.addTagsItem(x)
    }

    fun parameter(block: ParameterModel.() -> Unit) {
        operation.addParametersItem(ParameterModel().apply(block).parameter)
    }

    fun requestBody(block: RequestBodyModel.() -> Unit) {
        operation.requestBody(RequestBodyModel().apply(block).requestBody)
    }

    fun response(code: String, block: ApiResponseModel.() -> Unit) {
        if (operation.responses == null) {
            operation.responses(ApiResponses())
        }
        val apiResponses = operation.responses
        val x = ApiResponseModel().apply(block)
        apiResponses.addApiResponse(code, x.apiResponse)
    }
}

class ParameterModel {
    val parameter: Parameter = Parameter()

    fun name(x: String) {
        parameter.name(x)
    }

    fun `in`(x: String) {
        parameter.`in`(x)
    }

    fun description(x: String) {
        parameter.description(x)
    }

    fun required(x: Boolean) {
        parameter.required(x)
    }

    fun deprecated(x: Boolean) {
        parameter.deprecated(x)
    }

    fun allowEmptyValue(x: Boolean) {
        parameter.allowEmptyValue(x)
    }

    fun schema(type: KClass<*>) {
        val converter = ModelConverters.getInstance()
        val resolved = converter.resolveAsResolvedSchema(AnnotatedType(type.java))
        if (components.schemas == null) components.schemas(LinkedHashMap())
        if (resolved.referencedSchemas.isEmpty()) {
            parameter.schema(resolved.schema)
        } else {
            parameter.schema(Schema<Any>().`$ref`(type.simpleName))
            components.schemas[type.simpleName] = resolved.schema
            components.schemas.putAll(resolved.referencedSchemas)
        }
    }
}

class RequestBodyModel {
    val requestBody: RequestBody = RequestBody()

    fun description(x: String) {
        requestBody.description(x)
    }

    fun required(x: Boolean) {
        requestBody.required(x)
    }

    fun mediaType(mediaType: String, block: MediaTypeModel.() -> Unit) {
        val x = MediaTypeModel().apply(block)
        if (requestBody.content == null) {
            requestBody.content(Content())
        }
        requestBody.content.addMediaType(mediaType, x.mediaType)
    }
}

class MediaTypeModel {
    val mediaType: MediaType = MediaType()

    fun schema(type: KClass<*>) {
        val converter = ModelConverters.getInstance()
        val resolved = converter.resolveAsResolvedSchema(AnnotatedType(type.java))
        if (components.schemas == null) components.schemas(LinkedHashMap())
        if (resolved.referencedSchemas.isEmpty()) {
            mediaType.schema(resolved.schema)
        } else {
            mediaType.schema(Schema<Any>().`$ref`(type.simpleName))
            components.schemas[type.simpleName] = resolved.schema
            components.schemas.putAll(resolved.referencedSchemas)
        }
    }
}

class ApiResponseModel {
    val apiResponse: ApiResponse = ApiResponse()

    fun mediaType(mediaType: String, block: MediaTypeModel.() -> Unit) {
        val x = MediaTypeModel().apply(block)
        if (apiResponse.content == null) {
            apiResponse.content(Content())
        }
        apiResponse.content.addMediaType(mediaType, x.mediaType)
    }

    fun description(x: String) {
        apiResponse.description(x)
    }
}
