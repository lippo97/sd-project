package it.unibo.lpaas.core

@JvmInline
value class Tag(val value: String)

interface UseCase<T> {
    val tag: Tag

    suspend fun execute(): T

    fun <B> map(f: (T) -> B): UseCase<B> = UseCase.of(tag) { f(execute()) }

    fun <B> `as`(b: B): UseCase<B> = map { b }

    fun void(): UseCase<Unit> = `as`(Unit)

    companion object {
        fun <T> of(tag: Tag, execute: suspend () -> T): UseCase<T> = object : UseCase<T> {
            override val tag: Tag = tag

            override suspend fun execute(): T = execute()
        }

        fun <T> of(tag: String, execute: suspend () -> T): UseCase<T> = of(Tag(tag), execute)
    }
}
