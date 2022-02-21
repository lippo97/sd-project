package it.unibo.lpaas.client.streams

import io.kotest.core.spec.style.FunSpec
import io.mockk.spyk
import io.mockk.verify
import io.vertx.core.Vertx

internal class MappedStreamTest : FunSpec({
    val vertx = Vertx.vertx()

    @Suppress("EmptyFunctionBlock", "UnusedPrivateMember")
    class Test {
        fun f(n: String) {}
    }

    context("MappedStream") {
        test("it should map elements") {
            val test = spyk(Test())
            val stream = BoundedStream<Int>(vertx)
            stream.map { "${it * 2}" }
                .handler(test::f)

            vertx.executeBlocking<Void> {
                (0..50).forEach {
                    stream.write(it)
                }
                it.complete()
            }

            verify {
                (0..50)
                    .map { "${it * 2 }" }
                    .forEach(test::f)
            }
        }
    }
})
