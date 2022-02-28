package it.unibo.lpaas.delivery.http.exception

import it.unibo.lpaas.http.databind.MimeType

sealed class DeliveryException : Throwable()

class UnauthorizedException : DeliveryException()

class UnsupportedMediaTypeException(contentType: MimeType) : DeliveryException() {
    override val message: String = "Content type $contentType is not supported."
}

class ValidationException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : DeliveryException()
