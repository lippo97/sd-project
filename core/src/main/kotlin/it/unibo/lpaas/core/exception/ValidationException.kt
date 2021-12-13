package it.unibo.lpaas.core.exception

class ValidationException(override val message: String = "") : NonFatalError()
