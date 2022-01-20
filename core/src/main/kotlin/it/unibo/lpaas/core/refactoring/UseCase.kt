package it.unibo.lpaas.core.refactoring

fun interface UseCase<A> {
    suspend fun execute(): A

    fun <B> map(f: suspend (A) -> B): UseCase<B> = UseCase { f(execute()) }

    fun <B> flatMap(f: suspend (A) -> UseCase<B>): UseCase<B> = UseCase {
        f(execute())
            .execute()
    }

    fun <B> `as`(b: B): UseCase<B> = map { b }

    fun void(): UseCase<Unit> = `as`(Unit)
}
