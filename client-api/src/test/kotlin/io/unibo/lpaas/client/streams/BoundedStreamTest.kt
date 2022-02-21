package io.unibo.lpaas.client.streams

import io.kotest.core.spec.style.FunSpec
import io.mockk.spyk
import io.mockk.verify
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import it.unibo.lpaas.client.streams.BoundedStream

class BoundedStreamTest : FunSpec({
    val vertx = Vertx.vertx()

    @Suppress("EmptyFunctionBlock", "UnusedPrivateMember")
    class Test {
        fun f(n: Int) {}
    }

    context("BoundedStreamTest") {
        context("test read/write") {
            test("0..50") {
                val test = spyk(Test())
                val stream = BoundedStream<Int>(vertx)
                    .handler(test::f)

                vertx.executeBlocking<Void> {
                    (0..50).forEach {
                        stream.write(it)
                    }
                    it.complete()
                }

                verify {
                    (0..50).forEach(test::f)
                }
            }
            test("0..50 (with pause)") {
                val test = spyk(Test())
                val stream = BoundedStream<Int>(vertx)
                    .handler {
                        test.f(it)
                    }
                    .pause()

                vertx.executeBlocking<Void> {
                    (0..50).forEach {
                        stream.write(it)
                    }
                    it.complete()
                }.await()

                stream.resume()

                verify {
                    (0..50).forEach(test::f)
                }
            }
        }

        test("test pipe") {
            val test = spyk(Test())
            val src = BoundedStream<Int>(vertx)
            val dest = BoundedStream<Int>(vertx)
                .handler(test::f)

            src.pipeTo(dest)

            vertx.executeBlocking<Void> {
                (0..1023).forEach { src.write(it) }
            }

            verify {
                (0..1023).forEach(test::f)
            }
        }
    }
})
