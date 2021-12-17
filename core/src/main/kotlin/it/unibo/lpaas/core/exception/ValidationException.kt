package it.unibo.lpaas.core.exception

class ValidationException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : NonFatalError()
