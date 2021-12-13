package it.unibo.lpaas.core

interface UseCase<T> {
    val tag: String

    suspend fun execute(): T

    companion object {
        fun <T> of(tag: String, execute: suspend () -> T): UseCase<T> = object : UseCase<T> {
            override val tag: String = tag

            override suspend fun execute(): T = execute()
        }
    }
}
