package it.unibo.lpaas.core

interface UseCase<T> {
    val tag: String

    suspend fun execute(): T

    fun <B> map(f: (T) -> B): UseCase<B> = UseCase.of(tag) { f(execute()) }

    fun <B> `as`(b: B): UseCase<B> = map { b }

    fun void(): UseCase<Unit> = `as`(Unit)

    companion object {
        fun <T> of(tag: String, execute: suspend () -> T): UseCase<T> = object : UseCase<T> {
            override val tag: String = tag

            override suspend fun execute(): T = execute()
        }
    }
}
