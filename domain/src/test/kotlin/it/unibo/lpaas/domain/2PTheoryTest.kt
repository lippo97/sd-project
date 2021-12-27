package it.unibo.lpaas.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Terms
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory

val Struct.Companion.True: Struct
    get() = of(Terms.TRUE_FUNCTOR)

/**
 * This test suite was used to explore the existing 2p-kt [Theory] interface
 * and to gain experience with its behavior.
 *
 * Since this suite doesn't test any of our implementation we made it disabled
 * by default, but we keep it as a reference of how the [Theory] works.
 */
internal class `2PTheoryTest` : FunSpec({

    val theory = Theory.of(
        Clause.of(Struct.of("alberto")),
        Clause.of(Struct.of("marco", Struct.of("polo"))),
        Clause.of(Struct.of("marco", Struct.of("lino"))),
        Clause.of(Struct.of("marco", Struct.of("letto"))),
    )

    context("2p-kt Theory").config(enabled = true) {

        test("toString as Prolog text") {
            theory.toString(true) shouldBe """
            alberto :- true.
            marco(polo) :- true.
            marco(lino) :- true.
            marco(letto) :- true.

            """.trimIndent()
        }

        test("test custom clause print") {
            class WrappedClause(private val clause: Clause) {
                override fun toString(): String {
                    return if (clause.isFact) clause.head.toString()
                    else clause.toString()
                }
            }
            val clause = WrappedClause(Clause.of(Struct.of("alberto"), Struct.of("mario")))
            val clause2 = WrappedClause(Clause.of(Struct.of("alberto")))

            clause.toString() shouldBe "alberto :- mario"
            clause2.toString() shouldBe "alberto"
        }

        context("retract a clause") {
            test("it should use an anonymous variable") {
                val res = theory.retractAll(Struct.of("marco", Var.anonymous()))
                res.isSuccess shouldBe true
                res.shouldBeInstanceOf<RetractResult.Success<Theory>>()
                res.theory.clauses shouldHaveSize 1
                res.clauses shouldHaveSize 3
            }

            test("won't match any compound term using just the functor name") {
                val res = theory.retractAll(Struct.of("marco"))
                res.isFailure shouldBe true
                res.shouldBeInstanceOf<RetractResult.Failure<Theory>>()
                res.theory.clauses.zip(theory.clauses).forEach { (res, original) ->
                    res shouldBe original
                }
            }
        }

        context("assert a new clause") {
            test("it should prepend the fact") {
                val updated = theory.assertA(Struct.of("temperature", Struct.of("25°C")))
                updated.clauses shouldHaveSize 5
                updated.clauses.first() shouldBe Clause.of(
                    Struct.of("temperature", Struct.of("25°C")),
                    Struct.True
                )
            }
            test("it should append the fact") {
                val updated = theory.assertZ(Struct.of("temperature", Struct.of("25°C")))
                updated.clauses shouldHaveSize 5
                updated.clauses.last() shouldBe Clause.of(
                    Struct.of("temperature", Struct.of("25°C")),
                    Struct.True
                )
            }
            test("it should work passing duplicate clauses") {
                val aClause = Clause.of(Struct.of("alberto"), Struct.True)
                theory.clauses.first() shouldBe aClause
                val updated = theory.assertA(aClause)
                updated.clauses shouldHaveSize 5
                updated.clauses.take(2).shouldContainExactly(aClause, aClause)
            }
        }

        context("get facts in theory") {
            test("it should return a list of the facts") {
                theory.clauses
                    .filter { it.isFact }
                    .map { Fact(it.head.toString()) }
                    .shouldContainInOrder(
                        Fact("alberto"),
                        Fact("marco(polo)"),
                        Fact("marco(lino)"),
                        Fact("marco(letto)")
                    )
            }

            test("get facts by functor") {
                theory.clauses
                    .filter { it.isFact }
                    .filter { it.head?.functor == "marco" }
                    .map { Fact(it.head.toString()) }
                    .shouldContainInOrder(
                        Fact("marco(polo)"),
                        Fact("marco(lino)"),
                        Fact("marco(letto)")
                    )
            }
        }
    }
})
