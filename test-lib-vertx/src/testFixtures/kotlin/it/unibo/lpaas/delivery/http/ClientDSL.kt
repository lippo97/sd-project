package it.unibo.lpaas.delivery.http

import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json

@Suppress("LongParameterList")
private fun HttpClient.requestDSL(
    method: HttpMethod,
    requestURI: String,
    host: String = "localhost",
    port: Int = 8080,
    headers: MultiMap,
    body: (Json.() -> JsonObject)? = null,
): Future<HttpClientResponse> =
    request(
        RequestOptions().apply {
            this.method = method
            this.host = host
            this.uri = requestURI
            this.port = port
            this.headers = headers
        }
    ).flatMap { httpClientRequest ->
        return@flatMap if (body != null) {
            httpClientRequest.send(body(Json).toBuffer())
        } else {
            httpClientRequest.send()
        }
    }

fun HttpClient.get(
    requestURI: String,
    host: String = "localhost",
    port: Int = 8080,
    headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
    body: (Json.() -> JsonObject)? = null
): Future<HttpClientResponse> =
    requestDSL(HttpMethod.GET, requestURI, host, port, headers, body)

fun HttpClient.post(
    requestURI: String,
    host: String = "localhost",
    port: Int = 8080,
    headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
    body: (Json.() -> JsonObject)? = null
): Future<HttpClientResponse> =
    requestDSL(HttpMethod.POST, requestURI, host, port, headers, body)

fun HttpClient.patch(
    requestURI: String,
    host: String = "localhost",
    port: Int = 8080,
    headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
    body: (Json.() -> JsonObject)? = null
): Future<HttpClientResponse> =
    requestDSL(HttpMethod.PATCH, requestURI, host, port, headers, body)

fun HttpClient.put(
    requestURI: String,
    host: String = "localhost",
    port: Int = 8080,
    headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
    body: (Json.() -> JsonObject)? = null
): Future<HttpClientResponse> =
    requestDSL(HttpMethod.PUT, requestURI, host, port, headers, body)

fun HttpClient.delete(
    requestURI: String,
    host: String = "localhost",
    port: Int = 8080,
    headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
    body: (Json.() -> JsonObject)? = null
): Future<HttpClientResponse> =
    requestDSL(HttpMethod.DELETE, requestURI, host, port, headers, body)
