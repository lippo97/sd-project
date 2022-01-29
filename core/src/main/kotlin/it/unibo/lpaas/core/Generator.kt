package it.unibo.lpaas.core

/**
 * A random generator of [T]s.
 */
fun interface Generator<T> {
    fun generateRandom(): T
}
