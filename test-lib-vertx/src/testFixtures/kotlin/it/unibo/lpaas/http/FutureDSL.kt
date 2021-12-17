package it.unibo.lpaas.http

import io.vertx.core.Future

/**
 * Utility method that allows to inspect the future's current value without
 * modifying it.
 */
fun <A> Future<A>.tap(f: (A) -> Unit): Future<A> =
    map { f(it); it }
