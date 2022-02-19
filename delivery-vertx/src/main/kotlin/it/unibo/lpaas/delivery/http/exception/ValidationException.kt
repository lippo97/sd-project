package it.unibo.lpaas.delivery.http.exception

class ValidationException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : DeliveryException()
