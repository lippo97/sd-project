package it.unibo.lpaas.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct

internal class TheoryTest : FunSpec({

    val theory2p = it.unibo.tuprolog.theory.Theory.of(
        Clause.of(Struct.of("mario")),
        Clause.of(Struct.of("luigi")),
        Clause.of(Struct.of("daisy")),
        Clause.of(Struct.of("peach")),
    )

    context("Theory assertion (prepend)") {
        test("it should prepend the fact") {
            val data = Theory.Data(theory2p)
            data.assertA(Fact.of(Functor("bowser")))
                .value
                .clauses
                .first()
                // take the head of the clause
                .head shouldBe
                Fact.of(Functor("bowser")).fact
        }
    }

    context("Theory assertion (append)") {
        test("it should append the fact") {
            val data = Theory.Data(theory2p)
            data.assertZ(Fact.of(Functor("bowser")))
                .value
                .clauses
                .last()
                // take the head of the clause
                .head shouldBe
                Fact.of(Functor("bowser")).fact
        }
    }

    context("Simple atom retract") {
        test("it should return the updated data") {
            val data = Theory.Data(theory2p)
            data.retract(Functor("mario"), 0)
                .value
                .clauses
                .map(Clause::head)
                .shouldContainInOrder(
                    Fact.of(Functor("luigi")).fact,
                    Fact.of(Functor("daisy")).fact,
                    Fact.of(Functor("peach")).fact,
                )
        }

        test("it should keep the theory unchanged (arity not matching)") {
            val data = Theory.Data(theory2p)
            data.retract(Functor("mario"), 1)
                .value shouldBe data.value
        }

        test("it should keep the theory unchanged (functor not matching)") {
            val data = Theory.Data(theory2p)
            data.retract(Functor("bowser"), 0)
                .value shouldBe data.value
        }
    }

    context("Compound term retract") {
        val temperatureTheory = it.unibo.tuprolog.theory.Theory.of(
            Clause.of(Struct.of("temperature", Struct.of("25"))),
            Clause.of(Struct.of("temperature", Struct.of("21"))),
            Clause.of(Struct.of("temperature", Struct.of("22"))),
            Clause.of(Struct.of("temperature", Struct.of("23"))),
            Clause.of(Struct.of("temperature", Struct.of("23"), Struct.of("C"))),
            Clause.of(Struct.of("temperature")),
        )

        println(temperatureTheory.toString(true))

        test("it should retract only temperature/1") {
            val data = Theory.Data(temperatureTheory)
            data.retract(Functor("temperature"), 1)
                .value
                .clauses shouldHaveSize 2
        }

        test("it should retract only temperature/2") {
            val data = Theory.Data(temperatureTheory)
            data.retract(Functor("temperature"), 2)
                .value
                .clauses shouldHaveSize data.value.clauses.toList().size - 1
        }

        test("it should retract only temperature (atom)") {
            val data = Theory.Data(temperatureTheory)
            data.retract(Functor("temperature"), 0)
                .value
                .clauses shouldHaveSize data.value.clauses.toList().size - 1
        }
    }
})
