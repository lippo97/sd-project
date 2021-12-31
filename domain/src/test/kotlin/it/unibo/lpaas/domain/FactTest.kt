package it.unibo.lpaas.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct

internal class FactTest : FunSpec({
    context("Create Fact off existing Clause") {
        test("it should succeed when it holds an atom") {
            val fact = Fact.of(Clause.of(Struct.of("mario")))
            fact.arity shouldBe 0
            fact.functor shouldBe Functor("mario")
        }
        test("it should succeed when it holds a compound term") {
            val fact = Fact.of(Clause.of(Struct.of("marco", Struct.of("polo"))))
            fact.arity shouldBe 1
            fact.functor shouldBe Functor("marco")
        }
    }
    context("Create Fact off existing Struct") {
        test("it should succeed when it holds an atom") {
            val fact = Fact.of(Struct.of("mario"))
            fact.arity shouldBe 0
            fact.functor shouldBe Functor("mario")
        }
        test("it should succeed when it holds a compound term") {
            val fact = Fact.of(Struct.of("marco", Struct.of("polo")))
            fact.arity shouldBe 1
            fact.functor shouldBe Functor("marco")
        }
    }
})
