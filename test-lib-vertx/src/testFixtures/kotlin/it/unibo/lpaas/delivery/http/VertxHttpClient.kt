package it.unibo.lpaas.delivery.http

import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json

interface VertxHttpClient {

    fun request(
        method: HttpMethod,
        requestURI: String,
        headers: MultiMap,
        body: (Json.() -> JsonObject)? = null,
    ): Future<HttpClientResponse>

    fun get(
        requestURI: String,
        headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
        body: (Json.() -> JsonObject)? = null
    ): Future<HttpClientResponse> =
        request(HttpMethod.GET, requestURI, headers, body)

    fun post(
        requestURI: String,
        headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
        body: (Json.() -> JsonObject)? = null
    ): Future<HttpClientResponse> =
        request(HttpMethod.POST, requestURI, headers, body)

    fun put(
        requestURI: String,
        headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
        body: (Json.() -> JsonObject)? = null
    ): Future<HttpClientResponse> =
        request(HttpMethod.PUT, requestURI, headers, body)

    fun patch(
        requestURI: String,
        headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
        body: (Json.() -> JsonObject)? = null
    ): Future<HttpClientResponse> =
        request(HttpMethod.PATCH, requestURI, headers, body)

    fun delete(
        requestURI: String,
        headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
        body: (Json.() -> JsonObject)? = null
    ): Future<HttpClientResponse> =
        request(HttpMethod.DELETE, requestURI, headers, body)

    companion object {
        fun make(
            vertx: Vertx,
            host: String = "localhost",
            port: Int = 8080,
        ): VertxHttpClient = object : VertxHttpClient {
            val client = vertx.createHttpClient()

            override fun request(
                method: HttpMethod,
                requestURI: String,
                headers: MultiMap,
                body: (Json.() -> JsonObject)?
            ): Future<HttpClientResponse> = client.request(
                RequestOptions().apply {
                    this.method = method
                    this.host = host
                    this.uri = requestURI
                    this.port = port
                    this.headers = headers
                }
            ).flatMap { httpClientRequest ->
                if (body != null) {
                    httpClientRequest.send(body(Json).toBuffer())
                } else {
                    httpClientRequest.send()
                }
            }
        }
    }
}
