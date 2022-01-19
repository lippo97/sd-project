package it.unibo.lpaas.core

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

interface UseCase0<out R> : Tagged {
    suspend fun execute(): R
}

interface UseCase1<in T1, out R> : Tagged {
    suspend fun execute(t1: T1): R
}

interface UseCase2<in T1, in T2, out R> : Tagged {
    suspend fun execute(t1: T1, t2: T2): R
}

interface UseCase3<in T1, in T2, in T3, out R> : Tagged {
    suspend fun execute(t1: T1, t2: T2, t3: T3): R
}

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
