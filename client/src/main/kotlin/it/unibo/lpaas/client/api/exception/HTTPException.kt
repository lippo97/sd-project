package it.unibo.lpaas.client.api.exception

open class HTTPException(val statusCode: Int) : Throwable(message = "HTTP status code = $statusCode")
