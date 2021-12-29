package it.unibo.lpaas.collections

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.assertThrows

internal class NonEmptyListTest : FunSpec({
    context("Get on the NonEmptyList") {
        val nel = nonEmptyListOf("ciao", "come", "stai")

        test("it should access its members") {
            nel[0] shouldBe "ciao"
            nel[1] shouldBe "come"
            nel[2] shouldBe "stai"
        }

        test("it should throw exception") {
            assertThrows<IndexOutOfBoundsException> {
                nel[3]
            }
        }
    }
    context("Iteration on the NonEmptyList") {
        val nel = nonEmptyListOf("ciao", "come", "stai")

        test("it should access all its members") {
            nel.shouldContainInOrder("ciao", "come", "stai")
        }

        test("it should filter its members") {
            nel.filter { it != "come" }.shouldContainInOrder("ciao", "stai")
        }
    }
    test("It should never be empty") {
        nonEmptyListOf("ciao") shouldNot beEmpty()
        nonEmptyListOf("ciao", "come", "stai") shouldNot beEmpty()
    }
    test("Head should always be safe") {
        val ciao = nonEmptyListOf("ciao").head
        ciao shouldNotBe null
    }
})
