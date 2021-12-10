package it.unibo.lpaas.core.exception

sealed class DomainError : Throwable()

abstract class NonFatalError : DomainError()

class FatalError(throwable: Throwable) : DomainError() {
    override val cause: Throwable = throwable

    override val message: String? = throwable.message
}
