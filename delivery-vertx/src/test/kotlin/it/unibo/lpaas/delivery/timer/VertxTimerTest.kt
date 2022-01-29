package it.unibo.lpaas.delivery.timer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.mpp.timeInMillis
import io.mockk.Called
import io.mockk.spyk
import io.mockk.verify
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.core.timer.Timer

internal class VertxTimerTest : FunSpec({

    @Suppress("EmptyFunctionBlock")
    class Test {
        fun f() {}
    }

    suspend fun doAsync(fn: (done: () -> Unit) -> Unit) {
        val isComplete = Promise.promise<Unit>()
        fn(isComplete::complete)
        isComplete.future().await()
    }

    context("VertxTimer") {
        val vertx = Vertx.vertx()
        val timer = Timer.vertx(vertx)

        test("setTimeout") {
            doAsync { done ->
                val currentMillis = timeInMillis()
                val isComplete = Promise.promise<Unit>()
                val id = timer.setTimeout(2000) {
                    (timeInMillis() - currentMillis) shouldBeGreaterThan 2000
                    done()
                }
            }
        }

        test("setInterval") {
            doAsync { done ->
                val test = spyk(Test())
                val id = timer.setInterval(300) {
                    test.f()
                }
                timer.setTimeout(1000) {
                    verify(atLeast = 3) {
                        test.f()
                        done()
                    }
                }
            }
        }

        test("clear") {
            doAsync { done ->
                val test = spyk(Test())
                val id = timer.setTimeout(1000) {
                    test.f()
                }
                timer.clear(id)
                timer.setTimeout(1000) {
                    verify { test.f() wasNot Called }
                }
                done()
            }
        }
    }
})
