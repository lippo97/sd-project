package it.unibo.lpaas.collections

import it.unibo.lpaas.collections.impl.EnhancedIteratorImpl
import kotlin.coroutines.CoroutineContext

interface EnhancedIterator<T> : Iterator<T> {
    fun onCompletion(fn: suspend () -> Unit): EnhancedIterator<T>

    companion object {
        fun <T> of(
            iterator: Iterator<T>,
            coroutineContext: CoroutineContext
        ): EnhancedIterator<T> = EnhancedIteratorImpl(iterator, coroutineContext)
    }
}

fun <T> Iterator<T>.onCompletion(
    coroutineContext: CoroutineContext,
    fn: suspend () -> Unit
): EnhancedIterator<T> =
    EnhancedIterator.of(this, coroutineContext)
        .onCompletion { fn() }
