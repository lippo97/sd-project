package it.unibo.lpaas.collections.impl

import it.unibo.lpaas.collections.EnhancedIterator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EnhancedIteratorImpl<T>(
    private val iterator: Iterator<T>,
    private val coroutineContext: CoroutineContext
) : EnhancedIterator<T>, AbstractIterator<T>() {
    private val completionHandlers = mutableListOf<suspend () -> Unit>()

    @Synchronized
    override fun onCompletion(fn: suspend () -> Unit): EnhancedIterator<T> = this.apply {
        completionHandlers.add(fn)
    }

    @Synchronized
    override fun computeNext() {
        if (iterator.hasNext()) {
            setNext(iterator.next())
        } else {
            completionHandlers.forEach { GlobalScope.launch(coroutineContext) { it() } }
            done()
        }
    }
}
