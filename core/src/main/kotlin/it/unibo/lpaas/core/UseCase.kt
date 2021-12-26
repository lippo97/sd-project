package it.unibo.lpaas.core

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

@JvmInline
value class Tag(val value: String)

abstract class UseCase<T>(
    @get:JvmName("getTag") val tag: Tag,
) {

    abstract suspend fun execute(): T

    fun completableFuture(): CompletableFuture<T> =
        GlobalScope.future { execute() }

    fun completableFuture(context: CoroutineContext): CompletableFuture<T> =
        GlobalScope.future(context) { execute() }

    fun <B> map(f: (T) -> B): UseCase<B> = UseCase.of(tag) { f(execute()) }

    fun <B> `as`(b: B): UseCase<B> = map { b }

    @JvmName("asVoid")
    fun void(): UseCase<Unit> = `as`(Unit)

    companion object {
        fun <T> of(tag: Tag, execute: suspend () -> T): UseCase<T> = object : UseCase<T>(tag) {
            override suspend fun execute(): T = execute()
        }
    }
}
