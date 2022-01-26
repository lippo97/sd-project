package it.unibo.lpaas.collections.impl

import io.kotest.core.spec.style.FunSpec
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.GlobalScope

internal class EnhancedIteratorImplTest : FunSpec({

    @Suppress("EmptyFunctionBlock", "UnusedPrivateMember")
    class Test {
        fun f(int: Int) { }

        fun onCompletion() { }
    }

    context("EnhancedIteratorImpl") {
        test("it should return all its elements") {
            val mock = spyk(Test())
            EnhancedIteratorImpl((0..9).iterator(), GlobalScope.coroutineContext)
                .forEach(mock::f)
            (0..9).forEach {
                verify { mock.f(it) }
            }
        }
        test("it should call onCompletion function on completion") {
            val mock = spyk(Test())
            EnhancedIteratorImpl((0..9).iterator(), GlobalScope.coroutineContext)
                .onCompletion(mock::onCompletion)
                .forEach(mock::f)

            (0..9).forEach {
                verify { mock.f(it) }
            }
            verify { mock.onCompletion() }
        }
    }
})
