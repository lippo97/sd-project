package it.unibo.lpaas.delivery.timer

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import it.unibo.lpaas.core.timer.Timer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VertxTimer(private val vertx: Vertx) : Timer<Long> {
    override fun setTimeout(time: Long, fn: suspend () -> Unit): Long =
        vertx.setTimer(time) {
            GlobalScope.launch(vertx.dispatcher()) {
                fn()
            }
        }

    override fun setInterval(time: Long, fn: suspend () -> Unit): Long =
        vertx.setPeriodic(time) {
            GlobalScope.launch(vertx.dispatcher()) {
                fn()
            }
        }

    override fun clear(id: Long) {
        vertx.cancelTimer(id)
    }
}

fun Timer.Companion.vertx(vertx: Vertx): Timer<Long> = VertxTimer(vertx)
