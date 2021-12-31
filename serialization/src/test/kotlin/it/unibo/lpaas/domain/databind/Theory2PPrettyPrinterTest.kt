package it.unibo.lpaas.domain.databind

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.theory.parsing.ClausesParser

class Theory2PPrettyPrinterTest : FunSpec({
    context("When serializing a theory") {

        val theorySrc = """
        go(out) :- weather(sunny).
        stay(home) :- weather(rainy).
        """.trimIndent()

        val theory = ClausesParser.withDefaultOperators.parseTheory(theorySrc)

        test("it should print the clauses separated by line-break") {

            Theory2PPrinter.prettyPrinter().display(theory) shouldBe """
            go(out) :- weather(sunny).
            stay(home) :- weather(rainy).
            
            """.trimIndent()
        }

        test("it should not print the body of the truthy rules") {
            Theory2PPrinter.prettyPrinter().display(
                ClausesParser.withDefaultOperators.parseTheory(
                    """
                    $theorySrc
                    play(videogames).
                    watch(tv) :- true.
                    programming(c_sharp) :- false.
                
                    """.trimIndent()
                )
            ) shouldBe theorySrc + "\n" + """
            play(videogames).
            watch(tv).
            programming(c_sharp) :- false.

            """.trimIndent()
        }
    }
})
